package com.clj.fastble.libs.config;

public class Constants {
    public static double mLatitude = 0.0; // 维度
    public static double mLongitude = 0.0; // 经度
    public static long mMinRssi = -75; // 最小RSSI 默认-75
    public static double mWarningPower = 2.70; // 默认低电压警告
    public static long mTapNum = 20000; // 默认按钮次数报警值

    public static String mBleName = "MS360"; // 扫描蓝牙设备的名称

    public static final String UUID_SERVICE_READ = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"; // 读取
    public static final String UUID_SERVICE_WRITE = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"; // 写入
    public static final String characteristic_uuid = "0000FFF1-0000-1000-8000-00805F9B34FB"; // 字符 0x001E 0x2902
//    public static final String characteristic_uuid = "00002902-0000-1000-8000-00805f9b34fb"; // 字符 0x001E 0x2902
//    public static final String characteristic_uuid = "0x2902"; // 字符 0x001E 0x2902


}
