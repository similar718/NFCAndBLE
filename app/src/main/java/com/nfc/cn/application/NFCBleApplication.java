package com.nfc.cn.application;

import android.content.Context;

import com.clc.baselibs.base.BaseApplication;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import androidx.multidex.MultiDex;

public class NFCBleApplication extends BaseApplication {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //运行时多Dex加载， 继承MultiDexApplication最终也是调用这个方法
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        Beta.installTinker();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(this, "cac7a04aa1", false);
    }
}