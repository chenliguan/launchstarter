package com.launch.starter.tasks.delay;

import com.launch.starter.task.MainTask;

public class DelayInitTaskB extends MainTask {

    @Override
    public void run() {
        // 模拟一些操作
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
