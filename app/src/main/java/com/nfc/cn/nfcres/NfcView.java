package com.nfc.cn.nfcres;

public interface NfcView {

    void appendResponse(String response);
    void notNfcDevice();
    void notOpenNFC();
    void getNFCStatusOk();
}
