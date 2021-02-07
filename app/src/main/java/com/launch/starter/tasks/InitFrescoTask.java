package com.launch.starter.tasks;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.launch.starter.net.FrescoTraceListener;
import com.launch.starter.task.Task;

import java.util.HashSet;
import java.util.Set;

public class InitFrescoTask extends Task {

    @Override
    public void run() {
        Set<RequestListener> listenerset = new HashSet<>();
        listenerset.add(new FrescoTraceListener());
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(mContext).setRequestListeners(listenerset)
                .build();
        Fresco.initialize(mContext,config);
    }
}
