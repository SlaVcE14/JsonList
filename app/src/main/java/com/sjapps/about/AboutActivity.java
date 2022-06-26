package com.sjapps.about;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class AboutActivity extends AppCompatActivity {

    ImageView logo;
    RecyclerView ListRV;
    ArrayList<AboutListItem> aboutListItems = new ArrayList<>();
    AboutListAdapter aboutListAdapter;
    final String STORE_PACKAGE_NAME = "com.sjapps.sjstore";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initialize();

        PackageManager manager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = manager.getApplicationInfo(getPackageName(), 0);
            logo.setImageDrawable(applicationInfo.loadIcon(manager));

            String Name = (String) manager.getApplicationLabel(applicationInfo);
            String Version = manager.getPackageInfo(getPackageName(), 0).versionName;

            aboutListItems.add(new AboutListItem("Name",Name));
            aboutListItems.add(new AboutListItem("Version",Version));

            aboutListAdapter = new AboutListAdapter(aboutListItems);
            ListRV.setAdapter(aboutListAdapter);
            ListRV.setLayoutManager(new LinearLayoutManager(this));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    void initialize(){
        logo = findViewById(R.id.logo);
        ListRV = findViewById(R.id.aboutList);
        if (CheckStoreIsInstalled()){
            findViewById(R.id.updateBtn).setVisibility(View.VISIBLE);
        }
    }

    public void CheckForUpdate(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(STORE_PACKAGE_NAME,STORE_PACKAGE_NAME + ".AppActivity"));
        if (intent != null){
            intent.putExtra("packageName", getPackageName());
            intent.putExtra("isInstalled",true);
            startActivity(intent);
        }
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