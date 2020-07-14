package com.nfc.cn.ble;

/**
 * 向蓝牙设备写入命令结果回调
 */
public interface SendDataCallback {
    void success();
    void fail();
}
