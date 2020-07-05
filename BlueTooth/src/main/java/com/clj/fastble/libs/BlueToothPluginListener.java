package com.clj.fastble.libs;

import com.clj.fastble.data.BleDevice;

public interface BlueToothPluginListener {
//    void scanDevice(int type);// 1 扫描到目标设备 0 未扫描到目标设备
//    void connDevice(int type, BleDevice bleDevice); // 0 开始连接 1 连接成功 2 连接失败 3 断开连接 4 连接的设备不是我需要的数据

    void initFailed(byte data);  //  初始化失败 需要配合相关操作之后再重新初始化
    void initSuccess(); // 初始化成功
    void scanDevice(); // 扫描到目标设备
    void scanDeviceMinRSSI(); // 扫描到目标设备 但是RSSI不在设定范围内
    void scanNotDevice(); // 未扫描到目标设备
    void startConnDevice(BleDevice bleDevice); // 开始连接
    void connSuccesDevice(BleDevice bleDevice); // 连接成功
    void connFailedDevice(BleDevice bleDevice); // 连接失败
    void disConnDevice(BleDevice bleDevice); // 断开连接
    void connNotDesDevice(BleDevice bleDevice); // 断开连接 连接的设备不是我需要的数据
    void getDeviceInfo(BleDeviceInfo info); // 获取到设备里面广播的数据信息
    void warningTapNum(); // 按键次数超限
    void warningPower(); // 电池电压低报警
    void replyDataToDeviceSuccess(); // 回复硬件蓝牙成功
    void replyDataToDeviceFailed(); // 回复硬件蓝牙失败
}
