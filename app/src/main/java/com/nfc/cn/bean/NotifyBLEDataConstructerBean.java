package com.nfc.cn.bean;

public class NotifyBLEDataConstructerBean {

    public String byte0; // 包头 1        FF
    public String byte1; // 客户代码 1     CD
    public String byte2; // 数据包类型 1
    public String byte3; // IP/端口 6
    public String byte4; // DevId 2
    public String byte5; // 电池电压 1
    public String byte6; // 经纬度坐标 8
    public String byte7; // 经纬度标识 1
    public String byte8; // 卫星数 1
    public String byte9; // MAC地址 6
    public String byte10; // 配置版本 1
    public String byte11; // 结束 1 0x9C

    public String getByte0() {
        return "包头：" + byte0;
    }

    public void setByte0(String byte0) {
        this.byte0 = byte0;
    }

    public String getByte1() {
        return "客户代码：" + byte1;
    }

    public void setByte1(String byte1) {
        this.byte1 = byte1;
    }

    public String getByte2() {
        return "数据包类型：" + byte2;
    }

    public void setByte2(String byte2) {
        this.byte2 = byte2;
    }

    public String getByte3() {
        return "IP/端口：" + byte3;
    }

    public void setByte3(String byte3) {
        this.byte3 = byte3;
    }

    public String getByte4() {
        return "DevId：" + byte4;
    }

    public void setByte4(String byte4) {
        this.byte4 = byte4;
    }

    public String getByte5() {
        return "电池电压：" + byte5;
    }

    public void setByte5(String byte5) {
        this.byte5 = byte5;
    }

    public String getByte6() {
        return "经纬度坐标：" + byte6;
    }

    public void setByte6(String byte6) {
        this.byte6 = byte6;
    }

    public String getByte7() {
        return "经纬度标识：" + byte7;
    }

    public void setByte7(String byte7) {
        this.byte7 = byte7;
    }

    public String getByte8() {
        return "卫星数：" + byte8;
    }

    public void setByte8(String byte8) {
        this.byte8 = byte8;
    }

    public String getByte9() {
        return "MAC地址：" + byte9;
    }

    public void setByte9(String byte9) {
        this.byte9 = byte9;
    }

    public String getByte10() {
        return "配置版本：" + byte10;
    }

    public void setByte10(String byte10) {
        this.byte10 = byte10;
    }

    public String getByte11() {
        return "结束标记：" + byte11;
    }

    public void setByte11(String byte11) {
        this.byte11 = byte11;
    }

    @Override
    public String toString() {
        return "NotifyBLEDataConstructerBean{" +
                "byte0='" + getByte0() + '\'' +
                ", byte1='" + getByte1() + '\'' +
                ", byte2='" + getByte2() + '\'' +
                ", byte3='" + getByte3() + '\'' +
                ", byte4='" + getByte4() + '\'' +
                ", byte5='" + getByte5() + '\'' +
                ", byte6='" + getByte6() + '\'' +
                ", byte7='" + getByte7() + '\'' +
                ", byte8='" + getByte8() + '\'' +
                ", byte9='" + getByte9() + '\'' +
                ", byte10='" + getByte10() + '\'' +
                ", byte11='" + getByte11() + '\'' +
                '}';
    }
}
