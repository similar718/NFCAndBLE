package com.clc.baselibs.base;

/**
 * @author tanlei
 * @date 2019/7/27
 * @describe
 */

public interface IView {
    void showLoading();
    void hideLoading();
    void showLoadFail(String msg);
}
