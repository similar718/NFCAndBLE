package com.nfc.cn.http;

import com.clc.baselibs.bean.BaseDataBean;

import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    public static final String baseUrl = "https://gateway.wepayez.com/";//正式
    // http://203.174.52.78:1123/AliAppPay/UnifiedOrderApp

    /**
     * 支付
     * @param sub_appid：SWP25324
     * @param appid：wx2421b1c4370ec43b
     * @param service：pay.alipay.app.intl
     * @return：注册信息
     */
    @POST("pay/gateway")
    Observable<BaseDataBean<Object>> register(@Query("sub_appid") String sub_appid, @Query("appid") String appid, @Query("service") String service);
}

