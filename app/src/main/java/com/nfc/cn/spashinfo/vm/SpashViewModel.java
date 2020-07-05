package com.nfc.cn.spashinfo.vm;

import com.clc.baselibs.base.BaseViewModel;
import com.clc.baselibs.bean.BaseDataBean;
import com.clc.baselibs.rxjava2.CommonDisposableObserver;
import com.clc.baselibs.rxjava2.ComposeUtil;
import com.nfc.cn.http.ApiRepertory;
import com.nfc.cn.http.ApiService;

import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class SpashViewModel extends BaseViewModel {

    public MutableLiveData<Object> mSosData;

    public SpashViewModel() {
        mSosData = new MutableLiveData<Object>();
    }

    public void getPayInfo() {
        ApiService mService = ApiRepertory.getInstance().getApiService();
        Observable mObservable = mService.register("SWP25324","wx2421b1c4370ec43b","pay.alipay.app.intl");
        mObservable.subscribeOn(Schedulers.io())
                .compose(ComposeUtil.compose(mView))
                .subscribeWith(new CommonDisposableObserver<BaseDataBean<Object>>(mView) {
                    @Override
                    public void onNext(BaseDataBean<Object> body) {

                    }
                });
    }
}
