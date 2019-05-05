package com.wehaicao.ijkplayerdemo.util;

public class TimeUtils {

    /**
     * 时分秒转成字符串 (时分秒)
     * 00:00:00
     * time  单位秒 s
     * @param isShowZeroHour 当小时为0时是否显示
     */
    public static String getHms(long time, boolean isShowZeroHour) {
        if (!isShowZeroHour && time <= 3600) {
            return getMs(time, true);
        }
        String hms = "";
        long hour = 0l, minutes = 0l, seconds = 0l;
        hour = time / 3600;
        minutes = (time / 60) % 60;
        seconds = time % 60;
        hms = String.format("%02d:%02d:%02d", hour, minutes, seconds);
        return hms;
    }

    /**
     * 时间转化成字符串 （分秒）
     * 00:00
     *
     * @param isShowZeroMinute 当分钟为0时是否显示
     */
    public static String getMs(long time, boolean isShowZeroMinute) {
        String ms;
        long minutes, seconds;
        minutes = (time / 60) % 60;
        seconds = time % 60;
        ms = String.format("%02d:%02d", minutes, seconds);
        if (!isShowZeroMinute && time <= 60) {
            return String.format("%02d", seconds);
        }
        return ms;
    }
}
