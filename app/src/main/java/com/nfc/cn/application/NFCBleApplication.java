package com.nfc.cn.application;

import android.content.Context;

import com.clc.baselibs.base.BaseApplication;

public class NFCBleApplication extends BaseApplication {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //运行时多Dex加载， 继承MultiDexApplication最终也是调用这个方法
//        MultiDex.install(this);
    }
}