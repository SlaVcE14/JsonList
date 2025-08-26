package com.sj14apps.jsonlist.core.controllers;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


public class WebManager {
    private OkHttpClient client;

    public void getFromUrl(String url, WebCallback webCallback){
        if (url.trim().isEmpty())
            return;

        if (!url.startsWith("http"))
            url = "https://" + url;

        createClientIfNeeded();
        Request request;
        try {
            // CodeQL [java/ssrf] Safe to ignore: this is a client-side app, user controls their own URLs.
            request = new Request.Builder()
                    .url(url)
                    .build();

        }catch (IllegalArgumentException e){
            webCallback.onInvalidURL();
            return;
        }

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                webCallback.onFailure();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() == null) {
                    webCallback.onFailure(response.code());
                    return;
                }
                webCallback.onResponse(response.body().string());
            }
        });
        webCallback.onStarted();
    }

    private void createClientIfNeeded(){
        if (client == null)
            client = new OkHttpClient();
    }

    public void shutdownClient() {
        if (client == null)
            return;
        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdown();
    }
    public interface WebCallback {
        void onStarted();
        void onInvalidURL();
        void onResponse(String data);
        void onFailure();
        void onFailure(int code);
    }
}
