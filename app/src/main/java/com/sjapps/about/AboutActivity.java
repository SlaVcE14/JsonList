package com.sjapps.about;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {

    final String STORE_PACKAGE_NAME = "com.sjapps.sjstore";

    ImageView logo;
    NestedScrollView nestedScrollView;
    RecyclerView ListRV,LibListRV;
    ArrayList<AboutListItem> appInfoItems = new ArrayList<>();
    ArrayList<AboutListItem> libsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initialize();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        nestedScrollView.startAnimation(animation);
        PackageManager manager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = manager.getApplicationInfo(getPackageName(), 0);
            logo.setImageDrawable(applicationInfo.loadIcon(manager));

            String Name = (String) manager.getApplicationLabel(applicationInfo);
            String Version = manager.getPackageInfo(getPackageName(), 0).versionName;

            appInfoItems.add(new AboutListItem("Name",Name));
            appInfoItems.add(new AboutListItem("Version",Version));
            libsItems = new LibraryList().getItems(this);
            setupList(appInfoItems,ListRV);
            setupList(libsItems,LibListRV);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setupList(ArrayList<AboutListItem> items, @NonNull RecyclerView view) {
        AboutListAdapter adapter = new AboutListAdapter(items);
        view.setNestedScrollingEnabled(false);
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }

    void initialize(){
        logo = findViewById(R.id.logo);
        ListRV = findViewById(R.id.aboutList);
        LibListRV = findViewById(R.id.LibrariesList);
        nestedScrollView = findViewById(R.id.nestedList);

        if (CheckStoreIsInstalled()){
            findViewById(R.id.updateBtn).setVisibility(View.VISIBLE);
        }
    }

    public void CheckForUpdate(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(STORE_PACKAGE_NAME,STORE_PACKAGE_NAME + ".AppActivity"));
        intent.putExtra("packageName", getPackageName());
        intent.putExtra("isInstalled",true);
        startActivity(intent);
    }

    public boolean CheckStoreIsInstalled(){
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(STORE_PACKAGE_NAME,0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void Back(View view) {
        finish();
    }
}