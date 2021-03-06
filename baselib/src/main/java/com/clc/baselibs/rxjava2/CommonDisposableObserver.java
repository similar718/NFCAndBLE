package com.clc.baselibs.rxjava2;

import com.clc.baselibs.base.IView;
import com.clc.baselibs.base.BaseApplication;
import com.clc.baselibs.utils.NetWorkUtils;

import java.net.ConnectException;

import io.reactivex.observers.DisposableObserver;

/**
 * 统一处理异常信息(用於Observer)
 *
 * @param <T>
 */
public abstract class CommonDisposableObserver<T> extends DisposableObserver<T> {
    private IView mView;

    public CommonDisposableObserver() {
    }

    public CommonDisposableObserver(IView mView) {
        this.mView = mView;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable instanceof QzdsException) {
            QzdsException q = (QzdsException) throwable;
            if (mView != null) {
                mView.showLoadFail(throwable.getMessage());
            }
        } else if (throwable instanceof ConnectException) {
            if (!NetWorkUtils.isNetConnected(BaseApplication.getInstance().getContext())) {
                if (mView != null) {
                    mView.showLoadFail("网络异常");
                }
            } else {
                if (mView != null) {
                    mView.showLoadFail("服务器异常");
                }
            }
        } else {
            if (mView != null) {
                mView.showLoadFail(throwable.getMessage());
            }
        }
    }

    @Override
    public void onComplete() {
    }
}
