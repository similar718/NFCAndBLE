package com.clc.baselibs.base;

import android.app.Application;
import android.content.Context;

import com.clc.baselibs.utils.CrashHandlerUtils;

public class BaseApplication extends Application {

    public static BaseApplication INSTANCE;

    public static BaseApplication getInstance() {
        return INSTANCE;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        CrashHandlerUtils crashHandler = CrashHandlerUtils.getInstance();
        crashHandler.init(this);
    }

    public Context getContext(){
        return getApplicationContext();
    }
}
