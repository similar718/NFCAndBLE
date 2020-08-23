package com.clj.fastble.libs.config;

import java.util.UUID;

public class Constants {
    public static double mLatitude = 0.0; // 维度
    public static double mLongitude = 0.0; // 经度
    public static long mMinRssi = -75; // 最小RSSI 默认-75
    public static double mWarningPower = 2.70; // 默认低电压警告
    public static long mTapNum = 20000; // 默认按钮次数报警值

    public static String mBleName = "Tv450u-EDC84D60"; // 扫描蓝牙设备的名称

//    public static final String UUID_SERVICE_READ = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"; // 读取
//    public static final String UUID_SERVICE_READ = "0000FFE0-0000-1000-8000-00805F9B34FB"; // 读取
//    public static final String UUID_SERVICE_WRITE = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"; // 写入
//    public static final String UUID_SERVICE_WRITE = "0000FFE5-0000-1000-8000-00805F9B34FB"; // 写入
    public static final String characteristic_uuid = "00002902-0000-1000-8000-00805F9B34FB";
//    public static final String UUID_SERVICE_ALL = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
//    public static final String UUID_SERVICE_ALL = "0000FFE0-0000-1000-8000-00805F9B34FB";

//    private UUID SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");//服务UUID 支持notify和indicate
//    private UUID WRITE_CHARACTERISTIC = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb");//写UUID 支持写

//    public static String uuid_service = "0000ffe0-0000-1000-8000-00805f9b34fb"; //  支持notify和indicate
//    public static String uuid_chara = "0000ffe4-0000-1000-8000-00805f9b34fb";

}
