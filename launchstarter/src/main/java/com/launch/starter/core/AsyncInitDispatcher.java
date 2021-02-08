package com.launch.starter.core;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.UiThread;

import com.launch.starter.sort.TaskSortUtil;
import com.launch.starter.stat.TaskStat;
import com.launch.starter.task.DispatchRunnable;
import com.launch.starter.task.Task;
import com.launch.starter.task.TaskCallBack;
import com.launch.starter.utils.LaunchStarterUtils;
import com.launch.starter.utils.LogUtils;
import com.launch.starter.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步启动器
 */
public class AsyncInitDispatcher {

    /**
     * 线程最长等待时间，避免ANR
     */
    private static final int WAIT_TIME = 10000;

    private volatile static AsyncInitDispatcher sInstance;


    private long mStartTime;
    private static Context sContext;
    private static boolean sIsMainProcess;
    private List<Future> mFutures = new ArrayList<>();
    private static volatile boolean sHasInit;

    private List<Task> mAllTasks = new ArrayList<>();
    private List<Class<? extends Task>> mAllTaskCls = new ArrayList<>();

    private volatile List<Task> mMainThreadTasks = new ArrayList<>();
    private CountDownLatch mCountDownLatch;

    /**
     * 保存需要 wait 的 Task 的数量
     */
    private AtomicInteger mNeedWaitCount = new AtomicInteger();
    /**
     * 调用了 wait 时还没结束 且 需要等待的非主线程的 Task
     */
    private List<Task> mNeedWaitTasks = new ArrayList<>();
    /**
     * 已经结束了的 Task
     */
    private volatile List<Class<? extends Task>> mFinishedTasks = new ArrayList<>(100);

    /**
     * 被依赖的 HashMap
     */
    private HashMap<Class<? extends Task>, ArrayList<Task>> mDependedHashMap = new HashMap<>();

    /**
     * 启动器分析的次数，统计下分析的耗时；
     */
    private AtomicInteger mAnalyseCount = new AtomicInteger();


    private AsyncInitDispatcher() {
    }

    /**
     * 初始化，在createInstance()前先调用
     *
     * @param context
     */
    public static void init(Context context) {
        if (context != null) {
            sContext = context;
            sHasInit = true;
            sIsMainProcess = Utils.isMainProcess(sContext);
        }
    }

    /**
     * 注意：每次获取的都是同一个对象
     *
     * @return
     */
    public static AsyncInitDispatcher createInstance() {
        if (!sHasInit) {
            throw new RuntimeException("必须先调用 AsyncInitDispatcher.init 进行初始化");
        }

        if (sInstance == null) {
            synchronized (AsyncInitDispatcher.class) {
                if (sInstance == null) {
                    sInstance = new AsyncInitDispatcher();
                }
            }
        }

        return sInstance;
    }

    /**
     * 添加任务
     *
     * @param task
     * @return
     */
    public AsyncInitDispatcher addTask(Task task) {
        if (task != null) {
            collectDepends(task);
            mAllTasks.add(task);
            mAllTaskCls.add(task.getClass());

            // 添加 非主线程 且需要 wait 的 Task。主线程不需要CountDownLatch也是同步的
            if (ifNeedWait(task)) {
                mNeedWaitTasks.add(task);
                mNeedWaitCount.getAndIncrement();
            }
        }

        return this;
    }

    /**
     * 收集依赖
     *
     * @param task
     */
    private void collectDepends(Task task) {
        if (task.dependsOn() != null && task.dependsOn().size() > 0) {

            LogUtils.i("遍历任务 " + task.getClass().getSimpleName() + " 父亲集合，数量：" + task.dependsOn().size());

            // 遍历当前Task依赖的Task集合
            for (Class<? extends Task> cls : task.dependsOn()) {
                if (mDependedHashMap.get(cls) == null) {
                    mDependedHashMap.put(cls, new ArrayList<Task>());
                }

                LogUtils.i("  父亲——key：" + cls.getSimpleName() + "，被孩子——list：" + task.getClass().getSimpleName() + " 依赖");

                mDependedHashMap.get(cls).add(task);
                if (mFinishedTasks.contains(cls)) {
                    task.satisfy();
                }
            }
        }
    }

    /**
     * 是否需要等待，主线程的任务本来就是阻塞的，所以不用管
     *
     * @param task
     * @return
     */
    private boolean ifNeedWait(Task task) {
        return !task.runOnMainThread() && task.needWait();
    }

    /**
     * 开始
     */
    @UiThread
    public void start() {
        mStartTime = System.currentTimeMillis();
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new RuntimeException("must be called from UiThread");
        }
        if (mAllTasks.size() > 0) {
            mAnalyseCount.getAndIncrement();
            LaunchStarterUtils.printDependedMsg(mNeedWaitCount, mDependedHashMap);
            // 拓扑排序，拿到排好序之后的任务
            mAllTasks = TaskSortUtil.getSortResult(mAllTasks, mAllTaskCls);
            mCountDownLatch = new CountDownLatch(mNeedWaitCount.get());

            sendAndExecuteAsyncTasks();

            executeTaskMain();
        }
    }

    /**
     * 取消任务
     */
    public void cancel() {
        for (Future future : mFutures) {
            future.cancel(true);
        }
    }

    /**
     * 发送并执行异步任务
     */
    private void sendAndExecuteAsyncTasks() {
        for (Task task : mAllTasks) {
            if (task.onlyInMainProcess() && !sIsMainProcess) {
                // x
                markTaskDone(task);
            } else {
                // 真实发送任务
                sendTaskReal(task);
            }
            task.setSend(true);
        }
    }

    /**
     * 真实发送任务
     *
     * @param task
     */
    private void sendTaskReal(final Task task) {
        if (task.runOnMainThread()) {
            // 添加主线程任务，待会执行
            mMainThreadTasks.add(task);

            // 在主线程 且 需要回调 = 执行
            if (task.needCallBack()) {
                task.setTaskCallBack(new TaskCallBack() {
                    @Override
                    public void call() {
                        TaskStat.markTaskDone();
                        task.setFinished(true);
                        satisfyChildren(task);
                        markTaskDone(task);

                        LogUtils.i("在主线程 且 需要回调 = 执行" + task.getClass().getSimpleName() + " 结束");
                    }
                });
            }
        } else {
            // (1)先发送非主线程的任务，是否执行取决于具体线程池
            Future future = task.runOn().submit(new DispatchRunnable(task, this));
            mFutures.add(future);
        }
    }

    /**
     * (2)再执行主线程的任务，防止主线程任务阻塞，导致子线程任务不能立刻执行
     */
    private void executeTaskMain() {
        mStartTime = System.currentTimeMillis();
        for (Task task : mMainThreadTasks) {
            new DispatchRunnable(task, this).run();
        }
    }

    /**
     * 直接执行任务（不推荐使用）
     *
     * @param task
     */
    public void executeTask(Task task) {
        if (ifNeedWait(task)) {
            mNeedWaitCount.getAndIncrement();
        }
        task.runOn().execute(new DispatchRunnable(task, this));
    }

    /**
     * 通知 Children（被依赖的任务）：一个前置(Children依赖的父亲)任务已完成
     *
     * @param launchTask
     */
    public void satisfyChildren(Task launchTask) {
        ArrayList<Task> arrayList = mDependedHashMap.get(launchTask.getClass());
        if (arrayList != null && arrayList.size() > 0) {
            for (Task task : arrayList) {
                task.satisfy();
            }
        }
    }

    /**
     * 标记已经完成的Task
     *
     * @param task
     */
    public void markTaskDone(Task task) {
        if (ifNeedWait(task)) {
            mFinishedTasks.add(task.getClass());
            mNeedWaitTasks.remove(task);
            mCountDownLatch.countDown();
            mNeedWaitCount.getAndDecrement();
        }
    }


    /**
     * 等待，阻塞主线程
     */
    @UiThread
    public void await() {
        try {
            if (LogUtils.isDebug()) {
                LogUtils.i("仍然有 " + mNeedWaitCount.get() + " 个需要等待的任务");
                for (Task task : mNeedWaitTasks) {
                    LogUtils.i("  需要等待的任务名: " + task.getClass().getSimpleName());
                }
            }

            if (mNeedWaitCount.get() > 0) {
                mCountDownLatch.await(WAIT_TIME, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
        }
    }

    public static Context getContext() {
        return sContext;
    }

    public static boolean isMainProcess() {
        return sIsMainProcess;
    }
}
