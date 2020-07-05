package com.nfc.cn.utils;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.nfc.cn.application.NFCBleApplication;

public class SPUtils {
    public static final String COMMON_KEY_VALUE = "tour";
    public static final String BLE_NAME = "blename";


    public static void putString(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
        sharedPreferences.edit().putString(key, value).commit();
    }

    public static String getString(String key) {
        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
        return sharedPreferences.getString(key, "");
    }

    public static void putInt(String key, int value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
        sharedPreferences.edit().putInt(key, value).commit();
    }

    public static int getInt(String key) {
        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
        return sharedPreferences.getInt(key, 0);
    }

    public static void putBoolean(String key, boolean value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean defualtValue) {
        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
        return sharedPreferences.getBoolean(key, defualtValue);
    }


//    public static void clear() {
//        SharedPreferences sharedPreferences = NFCBleApplication.getInstance().getContext().getSharedPreferences(COMMON_KEY_VALUE, 0);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        SPUtils.putBoolean("isFirst",true);
//        editor.commit();
//    }

}
