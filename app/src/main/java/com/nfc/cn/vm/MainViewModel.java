package com.nfc.cn.vm;

import com.clc.baselibs.base.BaseViewModel;

import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends BaseViewModel {

    public MutableLiveData<Object> mSosData;

    public MainViewModel() {
        mSosData = new MutableLiveData<Object>();
    }

    public void getPayInfo() {
    }
}
