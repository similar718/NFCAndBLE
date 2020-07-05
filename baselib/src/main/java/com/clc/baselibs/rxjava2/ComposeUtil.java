package com.clc.baselibs.rxjava2;

import com.clc.baselibs.base.IView;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ComposeUtil {

    public static <T> ObservableTransformer<T, T> compose(IView mIBaseView) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> {
                            mIBaseView.showLoading();
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(() -> mIBaseView.hideLoading());
            }
        };
    }
    public static <T> ObservableTransformer<T, T> compose(IView mIBaseView,boolean isShowLoading) {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> {
                            if(isShowLoading){
                                mIBaseView.showLoading();
                            }
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(() -> {
                            if(isShowLoading){
                                mIBaseView.hideLoading();
                            }
                        });
            }
        };
    }
    public static <T> ObservableTransformer<T, T> compose() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    public static <T> FlowableTransformer<T, T> composeFlowable(IView mIBaseView) {
        return new FlowableTransformer<T, T>() {
            @Override
            public Publisher<T> apply(Flowable<T> upstream) {
                return upstream
                        .doOnSubscribe(disposable -> {mIBaseView.showLoading();})
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(() -> mIBaseView.hideLoading());
            }
        };
    }

}
