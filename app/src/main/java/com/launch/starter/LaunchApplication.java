package com.launch.starter;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import androidx.multidex.MultiDex;

import com.launch.starter.bean.NewsItem;
import com.launch.starter.core.AsyncInitDispatcher;
import com.launch.starter.tasks.*;

public class LaunchApplication extends Application {

    private String mDeviceId;
    private static Application mApplication;
    private boolean DEV_MODE = true;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        AsyncInitDispatcher.init(this);
        AsyncInitDispatcher dispatcher = AsyncInitDispatcher.createInstance();
        dispatcher
                .addTask(new InitAMapTask())
                .addTask(new InitStethoTask())
                .addTask(new InitWeexTask())
                .addTask(new InitBuglyTask())
                .addTask(new InitFrescoTask())
                .addTask(new InitJPushTask())
                .addTask(new InitUmengTask())
                .addTask(new GetDeviceIdTask())
                .start();

        dispatcher.await();
        initStrictMode();
    }

    private void initStrictMode() {
        if (DEV_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectCustomSlowCalls() //API等级11，使用StrictMode.noteSlowCode
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()// or .detectAll() for all detectable problems
                    .penaltyLog() //在Logcat 中打印违规异常信息
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .setClassInstanceLimit(NewsItem.class, 1)
                    .detectLeakedClosableObjects() //API等级11
                    .penaltyLog()
                    .build());
        }
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public static Application getApplication() {
        return mApplication;
    }

}
