package com.launch.starter.tasks;

import com.launch.starter.task.Task;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

public class InitBuglyTask extends Task {

    @Override
    public List<Class<? extends Task>> dependsOn() {
        List<Class<? extends Task>> task = new ArrayList<>();
        task.add(InitUmengTask.class);
        task.add(InitAMapTask.class);
        return task;
    }

    @Override
    public void run() {
        CrashReport.initCrashReport(mContext, "注册时申请的APPID", false);
    }
}
