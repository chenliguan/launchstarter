package com.launch.starter.tasks;

import com.launch.starter.task.Task;

public class InitTailTask extends Task {

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
