package com.nfc.cn.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiRepertory {
    private Retrofit mRetrofit;
    private OkHttpClient okHttpClient;

    private ApiService mApiService;
    /**
     * 超时时间
     */
    public static final int DEFAULT_TIMEOUT = 60;

    private ApiRepertory() {
        okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                .addInterceptor(new HeaderInterceptor())
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(ApiService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }
    //创建单例
    public static class SingleonHolder {
        private static final ApiRepertory instance = new ApiRepertory();
    }

    //获取单例
    public static ApiRepertory getInstance() {
        return SingleonHolder.instance;
    }

    public ApiService getApiService() {
        if(mApiService==null){
            mApiService = mRetrofit.create(ApiService.class);
        }
        return mApiService;
    }
}