package com.clc.baselibs.base;

import android.os.Bundle;
import android.text.TextUtils;

import com.clc.baselibs.dialog.LoadingDialig;
import com.clc.baselibs.utils.ToastUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;

/**
 * @author tanlei
 * @date 2019/7/27
 * @describe
 */

public class BaseActivity extends AppCompatActivity implements IView {
    public static List<BaseActivity> list = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list.add(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    public LoadingDialig mLoadingDialig;

    @Override
    public void showLoading() {
        if (mLoadingDialig == null) {
            mLoadingDialig = new LoadingDialig(this, "正在加载");
        }
        mLoadingDialig.show();
    }

    @Override
    public void hideLoading() {
        if (mLoadingDialig != null) {
            mLoadingDialig.dismiss();
        }
    }

    @Override
    public void showLoadFail(String msg) {
        if(TextUtils.isEmpty(msg)){
            return;
        }
        if(msg.contains("timeout")){
            ToastUtils.showText(this,"网络连接超时");
        }else {
            ToastUtils.showText(this,msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        list.remove(this);
    }
}
