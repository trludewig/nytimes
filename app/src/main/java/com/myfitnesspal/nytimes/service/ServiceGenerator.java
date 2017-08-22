package com.myfitnesspal.nytimes.service;

import android.content.Context;

import com.myfitnesspal.nytimes.service.interceptors.NetworkInterceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Original Source borrowed from https://github.com/lawloretienne/Loop and modified slightly.
 * Uses OkHttpClient and Retrofit to build request/response.
 */
public class ServiceGenerator {

    private static Retrofit.Builder retrofitBuilder
            = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

    private static OkHttpClient defaultOkHttpClient
            = new OkHttpClient.Builder()
                .build();

    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl) {
        return createService(serviceClass, baseUrl, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, Interceptor networkInterceptor) {
        OkHttpClient.Builder okHttpClientBuilder = defaultOkHttpClient.newBuilder();

        if(networkInterceptor != null){
            okHttpClientBuilder.addNetworkInterceptor(networkInterceptor);
        }

        OkHttpClient modifiedOkHttpClient = okHttpClientBuilder
                .addInterceptor(getHttpLoggingInterceptor()).addInterceptor(networkInterceptor)
                .build();

        retrofitBuilder.client(modifiedOkHttpClient);
        retrofitBuilder.baseUrl(baseUrl);

        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

    private static HttpLoggingInterceptor getHttpLoggingInterceptor(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }
}

