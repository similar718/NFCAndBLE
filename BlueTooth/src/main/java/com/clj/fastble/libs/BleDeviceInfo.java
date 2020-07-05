package com.clj.fastble.libs;

/**
 * Created by Administrator on 2019/9/22.
 */

public class BleDeviceInfo {
    private String broadcast; // 广播指示
    private String uuid; // UUID
    private String length; // 长度
    private String type; // 类型
    private String devId; // Dev Id
    private String power; // 电量
    private String temperature; // 芯片温度
    private String num; // 按键次数
    private String mac; // Mac地址
    private String send; // 发射功率
    private String other;
    private int rssi; // 设备的rssi

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSend() {
        return send;
    }

    public void setSend(String send) {
        this.send = send;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    @Override
    public String toString() {
        return "BleDeviceInfo{" +
                "broadcast='" + broadcast + '\'' +
                ", uuid='" + uuid + '\'' +
                ", length='" + length + '\'' +
                ", type='" + type + '\'' +
                ", devId='" + devId + '\'' +
                ", power='" + power + '\'' +
                ", temperature='" + temperature + '\'' +
                ", num='" + num + '\'' +
                ", mac='" + mac + '\'' +
                ", send='" + send + '\'' +
                ", other='" + other + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
