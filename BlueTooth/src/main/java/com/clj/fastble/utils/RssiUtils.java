package com.clj.fastble.utils;

/**
 * A和n的值，需要根据实际环境进行检测得出
 */
public class RssiUtils {

    /** A 发射端和接收端相隔1米时的信号强度 */
    private static final double A_Value = 50;
    /** n 环境衰减因子 */
    private static final double n_Value = 2.5;

    /**
     * 根据Rssi的值，计算距离，单位m
     * @param rssi 信号强度，单位dB
     */
    public static double getLeDistance(int rssi) {
        double power = (Math.abs(rssi) - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }

    /**
     * 经典蓝牙强度
     * -50 ~ 0dBm  信号强
     * -70 ~ -50dBm    信号中
     * <-70dBm      信号弱
     */
    public static byte getBredrLevel(int rssi) {
        if(rssi > -50) {
            return 3;
        } else if(rssi > -70) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * 低功耗蓝牙分四级
     * -60 ~ 0     4
     * -70 ~ -60   3
     * -80 ~ -70   2
     * <-80         1
     */
    public static byte getLeLevel(int rssi) {
        if(rssi > -60) {
            return 4;
        } else if(rssi > -70) {
            return 3;
        } else if(rssi > -80) {
            return 2;
        } else {
            return 1;
        }
    }
}
