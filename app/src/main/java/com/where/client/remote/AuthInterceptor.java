package com.where.client.remote;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthInterceptor implements Interceptor {

    private final String username;

    private final String password;

    public AuthInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Authorization", Credentials.basic(username, password, StandardCharsets.UTF_8))
                .build();

        return chain.proceed(request);
    }
}
