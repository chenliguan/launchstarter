package com.launch.starter.utils;

/**
 * 启动时间计算工具类
 */
public class LaunchTimer {

    private static long sTime;

    public static void startRecord() {
        sTime = System.currentTimeMillis();
    }

    public static void endRecord() {
        endRecord("");
    }

    public static void endRecord(String msg) {
        long cost = System.currentTimeMillis() - sTime;
        LogUtils.i("启动时间计算工具类 名称 ：" + msg + "，消耗时间：" + cost);
    }
}
