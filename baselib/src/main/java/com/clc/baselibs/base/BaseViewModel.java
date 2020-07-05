package com.clc.baselibs.base;

import androidx.databinding.BaseObservable;

/**
 * @author tanlei
 * @date 2019/7/27
 * @describe
 */

public class BaseViewModel extends BaseObservable {
    protected IView mView;

    public IView getIView() {
        return mView;
    }

    public void setIView(IView mView) {
        this.mView = mView;
    }
}
