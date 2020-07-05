package com.clc.baselibs.bean;

public class PayInterFaceJsonType {
    private String mch_create_ip;
    private String out_trade_no;
    private String body;
    private String total_fee;

    public PayInterFaceJsonType() {
    }

    public PayInterFaceJsonType(String mch_create_ip, String out_trade_no, String body, String total_fee) {
        this.mch_create_ip = mch_create_ip;
        this.out_trade_no = out_trade_no;
        this.body = body;
        this.total_fee = total_fee;
    }

    public String getMch_create_ip() {
        return mch_create_ip;
    }

    public void setMch_create_ip(String mch_create_ip) {
        this.mch_create_ip = mch_create_ip;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }
}
