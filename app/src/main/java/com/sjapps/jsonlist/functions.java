package com.sjapps.jsonlist;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class functions {
    public static String timeFormat(Calendar c){
        if (c == null)
            return "N/A";
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d h:mm:ss a, yyyy");
        return dateFormat.format(c.getTime());
    }

    public static String timeFormatShort(Calendar c){
        if (c == null)
            return "N/A";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(c.getTime());
    }

    public static void setAnimation(Context context, @NonNull View view, @AnimRes int animationRes) {
        setAnimation(context,view,animationRes,null);
    }

    public static void setAnimation(Context context, @NonNull View view, @AnimRes int animationRes, Interpolator interpolator) {
        Animation animation = AnimationUtils.loadAnimation(context, animationRes);
        if (interpolator != null)
            animation.setInterpolator(interpolator);
        view.startAnimation(animation);
    }

    public static int dpToPixels(@NonNull Context context, float dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

}
