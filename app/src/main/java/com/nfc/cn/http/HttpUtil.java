package com.nfc.cn.http;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    private static OkHttpClient okHttpClient = new OkHttpClient();
    private static Request.Builder builder = new Request.Builder();

    /**
     * 发送OkHttp GET 请求的方法
     *
     * @param url      url形式参数
     * @param callback 回调
     */
    public static void sendOkHttpGetRequest(String url, Callback callback) {
        Request request = builder.url(url).build();
        okHttpClient.newCall(request).enqueue(callback);
    }


    /**
     * 发送OkHttp POST 请求的方法
     *
     * @param urlAddress  url地址
     * @param requestBody 请求体
     * @param callback    回调
     */
    public static void sendOkHttpPostRequest(String urlAddress, RequestBody requestBody, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(urlAddress).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static void sendMultipart(String urlAddress, RequestBody requestBody, Callback callback) {
        //这里根据需求传，不需要可以注释掉
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("title", "wangshu")
//                .addFormDataPart("image", "wangshu.jpg",
//                        RequestBody.create(MEDIA_TYPE_PNG, new File("/sdcard/wangshu.jpg")))
//                .build();
//        private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        Request request = new Request.Builder().header("Authorization", "Client-ID " + "...").url(urlAddress).post(requestBody).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}