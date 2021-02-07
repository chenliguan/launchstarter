package com.launch.starter.task;

import android.os.Process;

import androidx.core.os.TraceCompat;

import com.launch.starter.core.AsyncInitDispatcher;
import com.launch.starter.stat.TaskStat;
import com.launch.starter.utils.LogUtils;
import com.launch.starter.utils.LaunchStarterUtils;

/**
 * 任务真正执行的地方
 */
public class DispatchRunnable implements Runnable {

    private Task mTask;
    private AsyncInitDispatcher mAsyncInitDispatcher;

    public DispatchRunnable(Task task) {
        this.mTask = task;
    }

    public DispatchRunnable(Task task, AsyncInitDispatcher dispatcher) {
        this.mTask = task;
        this.mAsyncInitDispatcher = dispatcher;
    }

    @Override
    public void run() {
        TraceCompat.beginSection(mTask.getClass().getSimpleName());

        LogUtils.i("任务：" + mTask.getClass().getSimpleName() + " 开始执行：run");

        Process.setThreadPriority(mTask.priority());

        long startTime = System.currentTimeMillis();

        mTask.setWaiting(true);
        // 当前Task等待，让父亲Task先执行
        mTask.waitToSatisfy();

        long waitTime = System.currentTimeMillis() - startTime;
        startTime = System.currentTimeMillis();

        // 执行Task
        mTask.setRunning(true);
        mTask.run();

        // 不在主线程 或者 不需要回调 = 执行
        if (!mTask.runOnMainThread() || !mTask.needCallBack()) {
            TaskStat.markTaskDone();
            mTask.setFinished(true);
            if (mAsyncInitDispatcher != null) {
                mAsyncInitDispatcher.satisfyChildren(mTask);
                mAsyncInitDispatcher.markTaskDone(mTask);
            }

            // 打印出来Task执行结束的日志
            LaunchStarterUtils.printTaskLog(mTask, startTime, waitTime);
        }

        TraceCompat.endSection();
    }
}
