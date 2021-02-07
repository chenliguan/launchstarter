package com.launch.starter.task;

/**
 * 主线程task
 */
public abstract class MainTask extends Task {

    @Override
    public boolean runOnMainThread() {
        return true;
    }
}
