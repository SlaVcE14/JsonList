package com.sjapps.logs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.sj14apps.jsonlist.core.AppState;
import com.sjapps.jsonlist.FileSystem;
import com.sjapps.jsonlist.R;
import com.sjapps.jsonlist.functions;
import com.sjapps.library.customdialog.BasicDialog;
import com.sjapps.library.customdialog.ListDialog;
import com.sjapps.library.customdialog.MessageDialog;


import java.util.Calendar;

public class LogActivity extends AppCompatActivity {

    TextView logTxt;
    String exportFileName = "logFile.txt";
    int numberOfLogs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        EdgeToEdge.enable(this);
        logTxt = findViewById(R.id.logTxt);
        setLayoutBounds();
        update();

        AppState state = FileSystem.loadStateData(this);

        if (state.hasNewCrash()){
            state.setHasNewCrash(false);
            FileSystem.SaveState(this,new Gson().toJson(state));
        }

    }

    private void setLayoutBounds() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootView), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets insetsN = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            layoutParams.leftMargin = insets.left + insetsN.left;
            layoutParams.topMargin = insets.top;
            layoutParams.rightMargin = insets.right + insetsN.right;
            logTxt.setPadding(logTxt.getPaddingLeft(),logTxt.getPaddingTop(),logTxt.getPaddingRight(),insets.bottom + insetsN.bottom);
            v.setLayoutParams(layoutParams);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FileSystem.deleteTempFile(this,exportFileName);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public void Back(View view) {
        finish();
    }

    public void deleteLog(View view) {
        BasicDialog dialog = new BasicDialog();
        dialog.Delete(this,true)
                .setTitle(getString(R.string.delete_logs))
                .onButtonClick(() ->{
                    dialog.dismiss();
                    FileSystem.SaveLog(this,new Gson().toJson(new CrashLogs()));
                    update();
                })
                .show();
    }

    private void update() {

        CrashLogs logs = FileSystem.loadLogData(this);

        AppState state = FileSystem.loadStateData(this);

        if (state.hasCrashLogs() && logs.getLogs().isEmpty()){
            state.setHasCrashLogs(false);
            FileSystem.SaveState(this,new Gson().toJson(state));
        }
        StringBuilder log = new StringBuilder();

        numberOfLogs = logs.getLogs().size();

        log.append(getDeviceInfo());

        for (String s : logs.getLogs()){
            log.append(s);
        }
        logTxt.setText(log.toString());
    }

    private String getDeviceInfo(){

        String s = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            s += "\n App Version Name: " + pInfo.versionName;
            s += "\n App Version Code: " + pInfo.versionCode;
            s += "\n";
        } catch (PackageManager.NameNotFoundException ignored) {}
        s += "\n OS Version: " + System.getProperty("os.version") + " ("
                + Build.VERSION.INCREMENTAL + ")";
        s += "\n OS API Level: " + Build.VERSION.SDK_INT;
        s += "\n Device: " + Build.DEVICE;
        s += "\n Model (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")";
        s += "\n Manufacturer: " + Build.MANUFACTURER;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            s += "\n screenWidth: " + getWindowManager().getCurrentWindowMetrics().getBounds().width();
            s += "\n screenHeight: " + getWindowManager().getCurrentWindowMetrics().getBounds().height();
        }else {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            s += "\n screenWidth: " + metrics.widthPixels;
            s += "\n screenHeight: " + metrics.heightPixels;
        }

        s += "\n";

        return s;
    }

    public void shareLog(View view) {

        if (logTxt.getText().toString().equals("")) {
            Toast.makeText(this, R.string.file_is_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {getString(R.string.copy_logs_to_clipboard), getString(R.string.share_logs)};

        ListDialog dialog = new ListDialog();
        dialog.Builder(this,true)
                .setTitle(getString(R.string.chose_action))
                .setItems(options,(position, value) -> {
                    dialog.dismiss();
                    switch (position){
                        case 0: copyToClipboard();
                            break;
                        case 1: share();
                            break;
                    }
                });

        MessageDialog warningDialog = new MessageDialog();
        warningDialog.ErrorDialogBuilder(this,true)
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.reports_may_include_personal_info))
                .show();
        warningDialog.dialog.setOnDismissListener(dialogInterface -> {
            warningDialog.dismiss();
            dialog.show();
        });
    }

    private void share() {

        exportFileName = "logFile " + functions.timeFormatShort(Calendar.getInstance()) + ".txt";

        String appName = getResources().getString(R.string.app_name);

        Intent intentShare = new Intent(Intent.ACTION_SEND);
        intentShare.setType("text/plain");
        intentShare.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".logs.provider",
                FileSystem.createTempFile(this,logTxt.getText().toString(),exportFileName)));
        intentShare.putExtra(Intent.EXTRA_SUBJECT, appName + " Crash logs");
        intentShare.putExtra(Intent.EXTRA_TEXT,  numberOfLogs + " Crash log" + (numberOfLogs>1?"s":"") + " for " + appName);

        startActivity(Intent.createChooser(intentShare,"Share file"));
    }

    private void copyToClipboard() {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("log",logTxt.getText().toString());
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(this, getString(R.string.logs_copied_to_clipboard), Toast.LENGTH_SHORT).show();
    }
}