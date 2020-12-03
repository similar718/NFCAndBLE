package com.nfc.cn.bean;

/**
 * 1、	设备的蓝牙数据包格式
 * 字段	        长度	            数据（Hex）	            说明
 * 包头	        1 Bytes	        FF
 * 客户代码	    1 Bytes	        CD	                两位BCD码，配置可修改
 * 数据包类型	1 Byte	        ……	                0x02-启动（坐标忽略）；0x03-停止
 * IP/端口	    6 Bytes	        ……	                IP-4字节；端口-2字节。由配置包设定
 * Dev ID1	    2 Bytes	        B4 B3 B2 B1	        由定位终端的MAC生成，如下图
 * 电池电压	    1 Bytes	        ……	                低四位为小数部分，高四位为整数部分
 * 经纬度坐标	8 Bytes	        ...	                纬度在前，经度在后。16进制，首字节为整数经纬度，小数点为后续3字节。例如：30.5683706 、103. 9622874
 * 经纬度标识	1 Byte 		                        NE=1（北纬东经）、SE=2（南纬东经）、NW=3（北纬西经）、SW=4（南纬西经）、无效坐标=5
 * 卫星数	    1 Byte
 * MAC地址	    6 Bytes	        ………	                蓝牙MAC地址，小端格式
 * 配置版本	    1 Byte		                        上报当前版本号，出厂设置为0
 * 结束	        1 Byte 	        0x9C	            数据包结束标记
 *
 * 总字节：29
 * 例：服务器配置IP：IP：192.168.1.1  端口: 9028
 *      0xC0 0xA8 0x01 0x01 -> 192 168 1 1 -> IP：192.168.1.1
 *      0x44 0x23 -> 0x2344 -> 9028
 * Dev ID1生成规则：
 *      MAC地址： AC DE 48 00 00 80
 *          B4: 第4个字节高位 00中的0
 *          B3: 第1个字节低位 AC中的C
 *          B2: 第5个字节低位 00中的0
 *          B1: 第3个字节高位 48中的4
 *      DEV ID: B4B3 B2B1 -> 0C 04
 *
 * MAC地址：
 *      0xFF 0xDD 0xCC 0x01 0x02 0x03
 *      数据段转换示意  030201ccddff
 * 坐标：
 *      0x67 0x00 0x00 0xFF -> 经度：整数0x67：103（十进制）、小数（由3个字节组合）0x0000FF：0000255（十进制）-> 经度：103.0000255
 *      0x1E 0x56 0xB9 0xFA -> 纬度：整数0x1E：30（十进制）、小数（由3个字节组合）0x56B9FA：5683706（十进制）-> 纬度：30.5683706
 */
public class NotifyBLEDataConstructerBean {

    public String baotou; // 包头 1        FF
    public String kehudaima; // 客户代码 1     CD
    public String shujubaoType; // 数据包类型 1 02 启动 03 停止
    public String ipAndPort; // IP/端口 6
    public String DevId; // DevId 2
    public String power; // 电池电压 1 低四位为小数部分 高四位为整数部分
    public String latlng; // 经纬度坐标 8  纬度在前，经度在后。16进制，首字节为整数经纬度，小数点为后续3字节。
    public String latlngType; // 经纬度标识 1 NE=1（北纬东经）、SE=2（南纬东经）、NW=3（北纬西经）、SW=4（南纬西经）、无效坐标=5
    public String weixingnum; // 卫星数 1
    public String mac; // MAC地址 6 蓝牙MAC地址，小端格式
    public String version; // 配置版本 1 上报当前版本号，出厂设置为0
    public String baowei; // 结束 1 0x9C

    public boolean mIsTest = false;

    public String getBaotou() {
        if (mIsTest) {
            return "包头：" + baotou;
        } else {
            return baotou;
        }
    }

    public void setBaotou(String baotou) {
        this.baotou = baotou;
    }

    public String getKehudaima() {
        if (mIsTest) {
            return "客户代码：" + kehudaima;
        } else {
            return kehudaima;
        }
    }

    public void setKehudaima(String kehudaima) {
        this.kehudaima = kehudaima;
    }

    public String getShujubaoType() {
        if (mIsTest) {
            return "数据包类型：" + shujubaoType;
        } else {
            return shujubaoType;
//            return shujubaoType.equals("02") ? "启动" : "停止";
        }
    }

    public void setShujubaoType(String shujubaoType) {
        this.shujubaoType = shujubaoType;
    }

    private int port;
    private String ipAddress;

    public int getIPPort(){
        return port;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public String getIpAndPort() { // 6
        if (mIsTest) {
            return "IP/端口：" + ipAndPort;
        } else {
            ipAddress = "";
            port = 0;
            for(int i = 0; i < 6; i++){
                int item = Integer.parseInt(ipAndPort.substring(i*2,i*2+2),16);
                if (i < 3) {
                    ipAddress += item + ".";
                } else if (i == 3){
                    ipAddress += item;
                } else if (i == 4){
                    port = item;
                } else {
                    port = port + item * 256;
                }
            }
            return ipAddress + ":" + port;
        }
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public String getDevId() {
        if (mIsTest) {
            return "DevId：" + DevId;
        } else {
            return DevId;
        }
    }

    public void setDevId(String devId) {
        this.DevId = devId;
    }

    public String getPower() { // 1 a5
        if (mIsTest) {
            return "电池电压：" + power;
        } else {
            int value = Integer.parseInt(power,16);
            return value / 16 + "." + value % 16;
        }
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getLatlng() { // 8
        if (mIsTest) {
            return "经纬度坐标：" + latlng;
        } else {
            String lng = "";
            String lat = "";
            for (int i = 0;i < 8; i++){
                int item = Integer.parseInt(latlng.substring(i*2,i*2+2),16);
                if (i == 0){
                    lng = item + ".";
                } else if (i == 4) {
                    lat = item + ".";
                } else if (i < 4) {
                    lng += item;
                } else {
                    lat += item;
                }
            }
            return lng + "," + lat;
        }
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getLatlngType() { // 经纬度标识 1 NE=1（北纬东经）、SE=2（南纬东经）、NW=3（北纬西经）、SW=4（南纬西经）、无效坐标=5
        if (mIsTest) {
            return "经纬度标识：" + latlngType;
        } else {
            int data = Integer.parseInt(latlngType,16);
            String result = "无坐标信息";
            switch (data) {
                case 1:
                    result = "北纬东经";
                    break;
                case 2:
                    result = "南纬东经";
                    break;
                case 3:
                    result = "北纬西经";
                    break;
                case 4:
                    result = "南纬西经";
                    break;
                case 5:
                    result = "无效坐标";
                    break;
            }
            return result;
        }
    }

    public void setLatlngType(String latlngType) {
        this.latlngType = latlngType;
    }

    public String getWeixingnum() {
        if (mIsTest) {
            return "卫星数：" + weixingnum;
        } else {
            return Integer.parseInt(weixingnum,16)+"";
        }
    }

    public void setWeixingnum(String weixingnum) {
        this.weixingnum = weixingnum;
    }

    public String getMac() {
        if (mIsTest) {
            return "MAC地址：" + mac;
        } else {
            // 将小端格式转换为大端格式
            String mac6 = mac.substring(0,2);
            String mac5 = mac.substring(2,4);
            String mac4 = mac.substring(4,6);
            String mac3 = mac.substring(6,8);
            String mac2 = mac.substring(8,10);
            String mac1 = mac.substring(10,12);
            String macw = mac1+mac2+mac3+mac4+mac5+mac6;
            return macw;
        }
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getVersion() {
        if (mIsTest) {
            return "配置版本：" + version;
        } else {
            return Integer.parseInt(version,16)+"";
        }
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBaowei() {
        if (mIsTest) {
            return "结束标记：" + baowei;
        } else {
            return baowei;
        }
    }

    public void setBaowei(String baowei) {
        this.baowei = baowei;
    }

    public boolean checkMacAndDevId(String macStr,String devId){
//        String macStr = getMac();
//        String devId = getDevId();
        // 大端格式
        char mac_b1 = macStr.charAt(4);
        char mac_b2 = macStr.charAt(9);
        char mac_b3 = macStr.charAt(1);
        char mac_b4 = macStr.charAt(6);
        // 小端
//        char mac_b1 = macStr.charAt(6);
//        char mac_b2 = macStr.charAt(3);
//        char mac_b3 = macStr.charAt(11);
//        char mac_b4 = macStr.charAt(4);

        char dev_b1 = devId.charAt(3);
        char dev_b2 = devId.charAt(2);
        char dev_b3 = devId.charAt(1);
        char dev_b4 = devId.charAt(0);

        if (mac_b1 == dev_b1 && mac_b2 == dev_b2 && mac_b3 == dev_b3 && mac_b4 == dev_b4){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "NotifyBLEDataConstructerBean{" +
                "包头='" + getBaotou() + '\'' +
                ", 客户代码='" + getKehudaima() + '\'' +
                ", 数据包类型='" + getShujubaoType() + '\'' +
                ", IP和端口='" + getIpAndPort() + '\'' +
                ", DevID='" + getDevId() + '\'' +
                ", 电源电压='" + getPower() + '\'' +
                ", 经纬度坐标='" + getLatlng() + '\'' +
                ", 经纬度标识='" + getLatlngType() + '\'' +
                ", 卫星数='" + getWeixingnum() + '\'' +
                ", mac='" + getMac() + '\'' +
                ", 版本号='" + getVersion() + '\'' +
                ", 包尾='" + getBaowei() + '\'' +
                '}';
    }
}
