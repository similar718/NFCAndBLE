package com.nfc.cn.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nfc.cn.MainActivity;
import com.nfc.cn.login.LoginActivity;

public class IntentManager {

    private final String TAG = "IntentManager";

    public static final int EDIT_ACTIVITY = 888;

    private IntentManager() {
    }

    public static final IntentManager getInstance() {
        return IntentManagerHolder.instance;
    }

    private void startActivity(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        context.startActivity(intent);
        // page jump anim
//        if (context instanceof Activity) {
//            ((Activity) context).overridePendingTransition(R.anim.anim_push_left_in,
//                    R.anim.anim_push_left_out);
//        }
    }

    public void startActivity(Context context, Class clz) {
        startActivity(context, new Intent(context, clz));
    }

    private void startAcitivityForResult(Activity context, Intent intent, int requestCode) {
        if (context == null) {
            return;
        }
        context.startActivityForResult(intent, requestCode);
    }

    public void goActivity(Context context, Intent intent) {
        startActivity(context, intent);
    }


    private static class IntentManagerHolder {
        private static final IntentManager instance = new IntentManager();
    }

    public void goMainActivity(Context context){
        startActivity(context, MainActivity.class);
    }


    public void goLoginActivity(Context context){
        startActivity(context, LoginActivity.class);
    }

}
