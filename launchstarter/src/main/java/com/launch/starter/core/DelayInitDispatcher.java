package com.launch.starter.core;

import android.os.Looper;
import android.os.MessageQueue;

import com.launch.starter.task.DispatchRunnable;
import com.launch.starter.task.Task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 延迟启动器
 */
public class DelayInitDispatcher {

    private final Queue<Task> mDelayTasks = new LinkedList<>();

    private final MessageQueue.IdleHandler mIdleHandler = new MessageQueue.IdleHandler() {
        @Override
        public boolean queueIdle() {
            if (mDelayTasks.size() > 0) {
                Task task = mDelayTasks.poll();
                new DispatchRunnable(task).run();
            }
            return !mDelayTasks.isEmpty();
        }
    };

    public DelayInitDispatcher addTask(Task task) {
        mDelayTasks.add(task);
        return this;
    }

    public void start() {
        Looper.myQueue().addIdleHandler(mIdleHandler);
    }
}
