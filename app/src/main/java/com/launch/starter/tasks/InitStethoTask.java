package com.launch.starter.tasks;

import android.os.Handler;
import android.os.Looper;

import com.facebook.stetho.Stetho;
import com.launch.starter.task.Task;

/**
 * 异步的Task
 */
public class InitStethoTask extends Task {

    /**
     * 是否需要尽快执行，解决特殊场景的问题：一个Task耗时非常多但是优先级却一般，很有可能开始的时间较晚，
     * 导致最后只是在等它，这种可以早开始。
     *
     * @return
     */
    @Override
    public boolean needRunAsSoon() {
        return true;
    }

    @Override
    public void run() {
        Handler handler = new Handler(Looper.getMainLooper());
        Stetho.initializeWithDefaults(mContext);
    }
}
