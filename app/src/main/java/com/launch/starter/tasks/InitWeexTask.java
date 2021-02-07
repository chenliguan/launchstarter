package com.launch.starter.tasks;

import android.app.Application;

import com.launch.starter.task.MainTask;
import com.taobao.weex.InitConfig;
import com.taobao.weex.WXSDKEngine;

/**
 * 主线程执行的task
 */
public class InitWeexTask extends MainTask {

    @Override
    public boolean needWait() {
        return false;
    }

    @Override
    public void run() {
        InitConfig config = new InitConfig.Builder().build();
        WXSDKEngine.initialize((Application) mContext, config);
    }
}
