package com.sjapps.jsonlist;

import android.transition.TransitionManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sjapps.jsonlist.core.JsonFunctions;
import com.sjapps.jsonlist.core.RawJsonView;

public class AndroidRawJsonView extends RawJsonView {

    MainActivity mainActivity;


    public AndroidRawJsonView(MainActivity mainActivity, int textColor, int keyColor, int numberColor, int booleanAndNullColor, int bgColor) {
        super(textColor, keyColor, numberColor, booleanAndNullColor, bgColor);
        this.mainActivity = mainActivity;
    }

    @Override
    public void open_closeSplitView() {
        TransitionManager.endTransitions(mainActivity.viewGroup);
        TransitionManager.beginDelayedTransition(mainActivity.viewGroup, mainActivity.autoTransition);

        if (showJson){
            functions.setAnimation(mainActivity,mainActivity.rawJsonRL,mainActivity.isVertical?R.anim.slide_bottom_out:R.anim.slide_right_out,new AccelerateDecelerateInterpolator());
            mainActivity.handler.postDelayed(()-> mainActivity.rawJsonRL.setVisibility(View.GONE),400);
            showJson = false;
            if (mainActivity.listRL.getVisibility() == View.GONE)
                mainActivity.listRL.setVisibility(View.VISIBLE);
            return;
        }
        showJson = true;
        mainActivity.rawJsonRL.setVisibility(View.VISIBLE);
        functions.setAnimation(mainActivity,mainActivity.rawJsonRL,mainActivity.isVertical?R.anim.slide_bottom_in:R.anim.slide_right_in,new DecelerateInterpolator());
        if (!mainActivity.isRawJsonLoaded)
            ShowJSON();
    }

    @Override
    public void ShowJSON() {
        if (mainActivity.data.getRawData().equals("-1")) {
            Snackbar.make(mainActivity.getWindow().getDecorView(), R.string.file_is_to_large_to_be_shown_in_a_split_screen, BaseTransientBottomBar.LENGTH_SHORT).show();
            if (mainActivity.progressView.getVisibility() == View.VISIBLE)
                mainActivity.loadingFinished(true);
            if (showJson)
                open_closeSplitView();
            return;
        }
        if (mainActivity.data.getRawData().equals(""))
            return;

        mainActivity.loadingStarted(mainActivity.getString(R.string.displaying_json));

        Thread thread = new Thread(() -> {
            String dataStr = JsonFunctions.getAsPrettyPrint(mainActivity.data.getRawData());
            mainActivity.handler.post(()-> {
                updateRawJson(dataStr);
                mainActivity.loadingFinished(true);
                mainActivity.isRawJsonLoaded = true;
            });
        });
        thread.setName("loadingJson");
        thread.start();
    }

    public void updateRawJson(String json) {
        String htmlData = mainActivity.rawJsonView.generateHtml(json,mainActivity.state);
        mainActivity.rawJsonWV.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }

}
