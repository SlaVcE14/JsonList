package com.sjapps.jsonlist.controllers;

import android.transition.TransitionManager;
import android.view.View;
import android.webkit.WebSettings;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sj14apps.jsonlist.core.JsonFunctions;
import com.sj14apps.jsonlist.core.controllers.RawJsonView;
import com.sjapps.jsonlist.databinding.ActivityMainBinding;

public class AndroidRawJsonView extends RawJsonView {

    MainActivity mainActivity;
    ActivityMainBinding mainBinding;


    public AndroidRawJsonView(MainActivity mainActivity, int textColor, int keyColor, int numberColor, int booleanAndNullColor, int bgColor) {
        super(textColor, keyColor, numberColor, booleanAndNullColor, bgColor);
        this.mainActivity = mainActivity;
        this.mainBinding = mainActivity.binding;
        setup();
    }

    private void setup(){
        WebSettings webSettings = mainBinding.rawJsonWV.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
    }

    @Override
    public void toggleSplitView() {
        TransitionManager.endTransitions(mainBinding.content);
        TransitionManager.beginDelayedTransition(mainBinding.content, mainActivity.autoTransition);

        if (showJson){
            if (mainActivity.isVertical)
                mainBinding.rawJsonRL.animate()
                        .translationY(mainBinding.rawJsonRL.getHeight())
                        .setDuration(400)
                        .withEndAction(()-> mainBinding.rawJsonRL.setVisibility(View.GONE))
                        .start();
            else mainBinding.rawJsonRL.animate()
                    .translationX(mainBinding.rawJsonRL.getWidth())
                    .setDuration(400)
                    .withEndAction(()-> mainBinding.rawJsonRL.setVisibility(View.GONE))
                    .start();


            mainBinding.resizeSplitViewBtn.animate()
                    .scaleX(.5f)
                    .scaleY(.5f)
                    .withEndAction(() -> mainBinding.resizeSplitViewBtn.setVisibility(View.GONE))
                    .setDuration(150)
                    .start();
            showJson = false;
            if (mainBinding.listRL.getVisibility() == View.GONE)
                mainBinding.listRL.setVisibility(View.VISIBLE);

            mainActivity.guideline.setGuidelinePercent(1f);
            return;
        }
        showJson = true;
        mainBinding.rawJsonRL.setVisibility(View.VISIBLE);

        mainActivity.guideline.setGuidelinePercent(0.5f);
        mainActivity.handler.postDelayed(()->{
                    mainBinding.resizeSplitViewBtn.setVisibility(View.VISIBLE);
                    mainBinding.resizeSplitViewBtn.animate()
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(150)
                            .start();
                },
                350);
        mainBinding.rawJsonRL.animate().cancel();

        mainBinding.rawJsonRL.animate()
                .translationY(0)
                .translationX(0)
                .setDuration(400)
                .start();

        if (!isRawJsonLoaded)
            ShowJSON();
    }

    @Override
    public void ShowJSON() {
        if (mainActivity.data.getRawData().equals("-1")) {
            Snackbar.make(mainActivity.getWindow().getDecorView(), R.string.file_is_to_large_to_be_shown_in_a_split_screen, BaseTransientBottomBar.LENGTH_SHORT).show();
            if (mainBinding.progressView.getVisibility() == View.VISIBLE)
                mainActivity.loadingFinished(true);
            if (showJson)
                toggleSplitView();
            return;
        }
        if (mainActivity.data.getRawData().equals(""))
            return;

        mainActivity.loadingStarted(mainActivity.getString(R.string.displaying_json));

        Thread thread = new Thread(() -> {
            String dataStr = JsonFunctions.getAsPrettyPrint(mainActivity.data.getRawData());
            updateRawJson(dataStr);
        });
        thread.setName("loadingJson");
        thread.start();
    }

    public void ShowDebugJson() {
        mainActivity.loadingStarted(mainActivity.getString(R.string.displaying_json));

        Thread thread = new Thread(() -> {
            String dataStr = JsonFunctions.getAsPrettyPrint(mainActivity.data.getRootNode().toString());
            updateRawJson(dataStr);
        });
        thread.setName("loadingJson");
        thread.start();
    }

    public void updateRawJson(String json) {
        String htmlData = generateHtml(json,mainActivity.state);
        mainActivity.handler.post(() -> {
            mainBinding.rawJsonWV.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
            if (mainActivity.isLoading)
                mainActivity.loadingFinished(true);
            isRawJsonLoaded = true;
        });
    }

}
