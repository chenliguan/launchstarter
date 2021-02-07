package com.launch.starter.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.launch.starter.LaunchApplication;
import com.launch.starter.task.MainTask;

public class GetDeviceIdTask extends MainTask {

    private String mDeviceId;

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        // 真正自己的代码
        TelephonyManager tManager = (TelephonyManager) mContext.getSystemService(
                Context.TELEPHONY_SERVICE);
        mDeviceId = tManager.getDeviceId();
        LaunchApplication app = (LaunchApplication) mContext;
        app.setDeviceId(mDeviceId);
    }
}
