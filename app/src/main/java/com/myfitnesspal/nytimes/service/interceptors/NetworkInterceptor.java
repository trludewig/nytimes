package com.myfitnesspal.nytimes.service.interceptors;

import android.content.Context;

import com.myfitnesspal.nytimes.exceptions.NetworkException;
import com.myfitnesspal.nytimes.util.Util;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by tludewig on 8/20/17.
 */
public class NetworkInterceptor implements Interceptor {

    private Context context;

    public NetworkInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!Util.isConnected(context)) {
            throw new NetworkException();
        }
        Request.Builder builder = chain.request().newBuilder();
        return chain.proceed(builder.build());
    }

}