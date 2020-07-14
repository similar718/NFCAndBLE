package com.nfc.cn.ble;

/**
 * 扫描蓝牙设备结果回调
 */
public interface ScanConnectDeviceCallback {

    void initFailed(byte data);  //  初始化失败 需要配合相关操作之后再重新初始化
    void initSuccess(); // 初始化成功
    void scanDevice(); // 扫描到目标设备
    void scanNotDevice(); // 未扫描到目标设备
    void startConnDevice(); // 开始连接

    void startConnNoSupport(); // 不支持

    void connSuccesDevice(); // 连接成功

    void getDeviceDataOriginal(String scanDeviceData); // 获取拿到最原始的数据 TODO 未做任何验证

    void checkDataIsFailure(String mac,String devID,String calDevID,String data); // 校验广播信息失败 devID 与mac不匹配

    void getDeviceData(String scanDeviceData); // 获取拿到的数据 并将数据更改一个位数 准备上传到服务器

    void getConnDeviceData(String scanDeviceData); // 获取已经连接之后的数据 并将数据更改一个位数 准备上传到服务器

    void getNotifyConnDeviceSuccess(String scanDeviceData); // 获取已经连接之后的数据 并将数据更改一个位数 准备上传到服务器
    void getNotifyConnDeviceFail(String scanDeviceData); // 获取已经连接之后的数据 并将数据更改一个位数 准备上传到服务器
    void getNotifyConnDeviceData(String scanDeviceData); // 获取已经连接之后的数据 并将数据更改一个位数 准备上传到服务器

    void connFailedDevice(); // 连接失败
    void disConnDevice(); // 断开连接
    void connNotDesDevice(); // 断开连接 连接的设备不是我需要的数据
    void replyDataToDeviceSuccess(String data); // 回复硬件蓝牙成功
    void replyDataToDeviceFailed(String data); // 回复硬件蓝牙失败
}
