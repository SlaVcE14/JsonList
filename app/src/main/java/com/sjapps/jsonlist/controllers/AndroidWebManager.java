package com.sjapps.jsonlist.controllers;

import android.webkit.WebSettings;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sjapps.jsonlist.core.controllers.WebManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AndroidWebManager implements WebManager {

    private final MainActivity activity;

    public AndroidWebManager(MainActivity activity) {
        this.activity = activity;
        setup();
    }

    private void setup(){
        WebSettings webSettings = activity.rawJsonWV.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
    }


    @Override
    public void getFromUrl(String url, WebCallback webCallback) {
        if (url.trim().isEmpty())
            return;

        if (!url.startsWith("http"))
            url = "https://" + url;

        OkHttpClient client = new OkHttpClient();
        Request request;
        try {
            request = new Request.Builder()
                    .url(url)
                    .build();

        }catch (IllegalArgumentException e){
            Toast.makeText(activity, activity.getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
            return;
        }

        activity.hideUrlSearchView();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                webCallback.onFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    webCallback.onFailure(response.code());
                    return;
                }
                webCallback.onResponse(response.body().string());

            }
        });
        activity.loadingStarted();
        activity.isUrlSearching = true;

    }
}
