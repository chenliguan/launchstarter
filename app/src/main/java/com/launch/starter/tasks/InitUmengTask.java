package com.launch.starter.tasks;

import com.launch.starter.task.Task;
import com.umeng.commonsdk.UMConfigure;

public class InitUmengTask extends Task {

    @Override
    public boolean needWait() {
        return true;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UMConfigure.init(mContext, "58edcfeb310c93091c000be2", "umeng",
                UMConfigure.DEVICE_TYPE_PHONE, "1fe6a20054bcef865eeb0991ee84525b");
    }
}
