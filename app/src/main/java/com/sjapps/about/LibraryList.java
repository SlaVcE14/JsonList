package com.sjapps.about;

import com.google.gson.internal.GsonBuildConfig;
import com.sjapps.library.BuildConfig;

public class LibraryList extends ListGenerator{
    @Override
    public void init() {
        addItem("SJ Dialog", BuildConfig.VERSION_NAME, "https://github.com/SlaVcE14/SJ-Dialog");
        addItem("gson", GsonBuildConfig.VERSION, "https://github.com/google/gson");
        addItem("core-splashscreen","1.0.1","https://developer.android.com/develop/ui/views/launch/splash-screen");
        addItem("material-components","1.12.0-alpha03","https://github.com/material-components/material-components-android");
    }
}

