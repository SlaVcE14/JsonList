package com.sj14apps.jsonlist.core.controllers;

public interface WebManager {

    void getFromUrl(String url, WebCallback webCallback);

    interface WebCallback {
        void onResponse(String data);
        void onFailure();
        void onFailure(int code);
    }
}
