package com.sjapps.jsonlist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    MaterialSwitch CheckForUpdateSw;
    MaterialSwitch disableMIMEFilterSw;
    Spinner ThemeSpinner;
    ArrayAdapter<CharSequence> Themes;
    AppState state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialize();

        LoadData();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        findViewById(R.id.mainSV).startAnimation(animation);

        ThemeSpinner.setSelection(state.getTheme());

        ThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state.setTheme(position);
                switch (position) {
                    case 0:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                    case 1:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                    case 2:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }

                SaveData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        CheckForUpdateSw.setChecked(state.isAutoCheckForUpdate());
        disableMIMEFilterSw.setChecked(state.isMIMEFilterDisabled());

        CheckForUpdateSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            state.setAutoCheckForUpdate(isChecked);
            SaveData();
        });

        disableMIMEFilterSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            state.setMIMEFilterDisabled(isChecked);
            SaveData();
        });

    }

    private void LoadData() {
        state = FileSystem.loadStateData(this);
    }

    private void SaveData() {
        FileSystem.SaveState(this, state);
    }

    private void initialize() {
        CheckForUpdateSw = findViewById(R.id.CheckForUpdateSwitch);
        disableMIMEFilterSw = findViewById(R.id.MIMESwitch);
        ThemeSpinner = findViewById(R.id.theme_spinner);
        Themes = ArrayAdapter.createFromResource(this, R.array.Themes, android.R.layout.simple_spinner_item);
        Themes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ThemeSpinner.setAdapter(Themes);

    }

    public void goBack(View view) {
        finish();
    }

}