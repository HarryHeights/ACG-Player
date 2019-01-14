/*
 * ************************************************************
 * 文件：HttpUtil.java  模块：geeklibrary  项目：MusicPlayer
 * 当前修改时间：2019年01月14日 14:45:09
 * 上次修改时间：2019年01月14日 08:14:05
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2019
 * ************************************************************
 */

package top.geek_stusio.chenlongcould.geeklibrary;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {

    public static void sedOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder().url(address).build();       //传入地址
        okHttpClient.newCall(request).enqueue(callback);                          //处理服务器响应
    }
}
