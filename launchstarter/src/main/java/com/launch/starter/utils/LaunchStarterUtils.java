package com.launch.starter.utils;

import android.os.Looper;

import com.launch.starter.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LaunchStarterUtils {

    /**
     * 输出排好序的Task
     *
     * @param tasks
     */
    public static void printSortTask(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            String taskName = tasks.get(i).getClass().getSimpleName();
            if (i != 0) {
                sb.append("--->");
            }
            sb.append(taskName);
        }
        LogUtils.e(sb.toString());
    }

    /**
     * 输出排好序的List
     *
     * @param lists
     */
    public static void printSortList(List<Integer> lists) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lists.size(); i++) {
            String taskName = lists.get(i).toString();
            if (i != 0) {
                sb.append("--->");
            }
            sb.append(taskName);
        }
        LogUtils.e(sb.toString());
    }

    /**
     * 查看被依赖的信息
     *
     * @param needWaitCount
     * @param dependedHashMap
     */
    public static void printDependedMsg(AtomicInteger needWaitCount,
                                        HashMap<Class<? extends Task>, ArrayList<Task>> dependedHashMap) {
        LogUtils.i("查看需要等待的数量: " + (needWaitCount.get()));

        if (LogUtils.isDebug()) {
            LogUtils.i("查看被依赖——父亲的信息：");
            for (Class<? extends Task> cls : dependedHashMap.keySet()) {
                LogUtils.i("  父亲：" + cls.getSimpleName() + "，有 " + dependedHashMap.get(cls).size() + " 个孩子");
                for (Task task : dependedHashMap.get(cls)) {
                    LogUtils.i("   被孩子：" + task.getClass().getSimpleName() + " 依赖");
                }
            }
        }
    }

    /**
     * 打印出来Task执行结束的日志
     *
     * @param task
     * @param startTime
     * @param waitTime
     */
    public static void printTaskLog(Task task, long startTime, long waitTime) {
        long runTime = System.currentTimeMillis() - startTime;
        if (LogUtils.isDebug()) {
            LogUtils.e("任务：" + task.getClass().getSimpleName() + " 结束。等待时间：" + waitTime + " ，执行时间："
                    + runTime + "，是否主线程： " + (Looper.getMainLooper() == Looper.myLooper())
                    + "，needWait：" + task.needWait()
                    + "，线程名：" + Thread.currentThread().getName()
            );
        }
    }
}
