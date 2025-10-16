package com.sjapps.about;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sj14apps.jsonlist.core.controllers.WebManager;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sjapps.library.customdialog.BasicDialog;
import com.sjapps.library.customdialog.CustomViewDialog;
import com.sjapps.library.customdialog.DialogButtonEvents;
import com.sjapps.library.customdialog.ImageListItem;
import com.sjapps.library.customdialog.ListDialog;
import com.sjapps.library.customdialog.SJDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    private static final String SITE_APP_VERSIONS = "https://slavce14.github.io/redirect?link=jsonlist-app-versions";
    private static final String RELEASE_NOTES_URL = "https://slavce14.github.io/apps/JsonList/changelogs.json";
    private static final String SITE_FDroid = "https://slavce14.github.io/redirect?link=jsonlist-fdroid";
    private static final String SITE_IzzyOnDroid = "https://slavce14.github.io/redirect?link=jsonlist-izzy";
    private static final String GITHUB_REPOSITORY_RELEASES = "https://github.com/SlaVcE14/JsonList/releases";
    final String STORE_PACKAGE_NAME = "com.sjapps.sjstore";
    final String CONTACT_MAIL = "slavce14.apps@gmail.com";

    ImageView logo;
    NestedScrollView nestedScrollView;
    RecyclerView ListRV,LibListRV;
    ArrayList<AboutListItem> appInfoItems = new ArrayList<>();
    ArrayList<AboutListItem> libsItems;
    boolean isStoreInstalled;
    Drawable storeIcon;
    boolean releaseNotesOpen = false;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        initialize();
        setLayoutBounds();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        nestedScrollView.startAnimation(animation);
        PackageManager manager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = manager.getApplicationInfo(getPackageName(), 0);
            logo.setImageDrawable(applicationInfo.loadIcon(manager));

            String Name = (String) manager.getApplicationLabel(applicationInfo);
            String Version = manager.getPackageInfo(getPackageName(), 0).versionName;

            appInfoItems.add(new AboutListItem(getString(R.string.name),Name));
            appInfoItems.add(new AboutListItem(getString(R.string.version),Version));
            libsItems = new LibraryList().getItems(this);
            setupList(appInfoItems,ListRV);
            setupList(libsItems,LibListRV);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
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
            View scrollRL = findViewById(R.id.scrollRL);
            scrollRL.setPadding(scrollRL.getPaddingLeft(),scrollRL.getPaddingTop(),scrollRL.getPaddingRight(),insets.bottom + insetsN.bottom);
            v.setLayoutParams(layoutParams);
            return WindowInsetsCompat.CONSUMED;
        });
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
            isStoreInstalled = true;
        }
    }

    public void CheckForUpdate(View view) {
        ListDialog dialog = new ListDialog();

        ArrayList<ImageListItem> items = new ArrayList<>();
        items.add(new ImageListItem("Site", AppCompatResources.getDrawable(this,R.drawable.ic_globe), (ImageItemClick) this::openSite));
        items.add(new ImageListItem("GitHub", AppCompatResources.getDrawable(this,R.drawable.github_logo), (ImageItemClick) this::openGitHub));
        items.add(new ImageListItem("F-Droid", AppCompatResources.getDrawable(this,R.drawable.fdroid_logo), (ImageItemClick) this::openFDroid));
        items.add(new ImageListItem("IzzyOnDroid", AppCompatResources.getDrawable(this, R.drawable.izzyondroid_logo), (ImageItemClick) this::openIzzy));

        if (isStoreInstalled) {
            items.add(new ImageListItem("SJ Store", storeIcon, (ImageItemClick) this::openStore));
        }


        dialog.Builder(this,true)
                .setTitle("Open...")
                .setImageItems(items, (position, obj) -> {
                    if (obj.getData() == null)
                        return;
                    ((ImageItemClick) obj.getData()).onClick();
                    dialog.dismiss();
                })
                .show();
    }
    
    private void openGitHub(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPOSITORY_RELEASES));
        startActivity(intent);
    }

    private void openSite(){
        openLink(SITE_APP_VERSIONS);
    }

    private void openFDroid(){
        openLink(SITE_FDroid);
    }

    private void openIzzy(){
        openLink(SITE_IzzyOnDroid);
    }

    private void openLink(String site){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(site));
        startActivity(intent);
    }

    private void openStore(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(STORE_PACKAGE_NAME,STORE_PACKAGE_NAME + ".AppActivity"));
        intent.putExtra("packageName", getPackageName());
        intent.putExtra("isInstalled",true);
        startActivity(intent);
    }

    private boolean CheckStoreIsInstalled() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(STORE_PACKAGE_NAME, 0);
            storeIcon = packageInfo.applicationInfo.loadIcon(packageManager);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void SendFeedback(View view) {

        BasicDialog dialog = new BasicDialog();
        dialog.Builder(this,true)
                .setTitle(getString(R.string.include_info))
                .setMessage(getString(R.string.include_info_description))
                .setLeftButtonText(getString(R.string.no))
                .setRightButtonText(getString(R.string.yes))
                .setMessageAlignment(SJDialog.TEXT_ALIGNMENT_CENTER)
                .onButtonClick(new DialogButtonEvents() {
                    @Override
                    public void onLeftButtonClick() {
                        sendMail(false);
                        dialog.dismiss();
                    }

                    @Override
                    public void onRightButtonClick() {
                        sendMail(true);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void sendMail(boolean sendInfo){
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL,new String[]{CONTACT_MAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT,"Json List Feedback:");
        if (sendInfo)
            intent.putExtra(Intent.EXTRA_TEXT,getInfo());
        startActivity(intent);
    }

    private String getInfo(){
        String s = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            s += "\n App Version Name: " + pInfo.versionName;
            s += "\n App Version Code: " + pInfo.versionCode;
            s += "\n";
        } catch (PackageManager.NameNotFoundException ignored) {}
        s += "\n OS API Level: " + Build.VERSION.SDK_INT;
        s += "\n Manufacturer: " + Build.MANUFACTURER;
        s += "\n Model (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")";
        s += "\n Device Version: " + Build.VERSION.INCREMENTAL;
        s += "\n";

        return s;
    }

    public void ShowReleasesNotes(View view) {
        if (releaseNotesOpen)
            return;

        releaseNotesOpen = true;
        CustomViewDialog dialog = new CustomViewDialog();
        ScrollView scrollView = new ScrollView(AboutActivity.this);
        TextView textView = new TextView(AboutActivity.this);

        WebManager webManager = new WebManager();
        webManager.getFromUrl(RELEASE_NOTES_URL, new WebManager.WebCallback() {
            @Override
            public void onStarted() {
                textView.setText(R.string.loading);
                textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                scrollView.addView(textView);
                dialog.Builder(AboutActivity.this,true)
                        .setTitle(getString(R.string.release_notes))
                        .addCustomView(scrollView)
                        .onDismissListener(dialog1 -> {
                            releaseNotesOpen = false;
                        })
                        .show();
            }

            @Override
            public void onInvalidURL() {

            }

            @Override
            public void onResponse(String data) {
                Type listType = new TypeToken<List<ReleaseNote>>() {}.getType();

                List<ReleaseNote> releasesHistory = new Gson().fromJson(data,listType);
                if (releasesHistory == null)
                    return;

                StringBuilder stringBuilder = new StringBuilder();
                for (ReleaseNote note : releasesHistory){
                    stringBuilder.append("<h3>").append(note.title).append("</h3>");
                    stringBuilder.append(note.changelog.replaceAll("\n","<br>")).append("<br>");
                    stringBuilder.append("<br><br>");
                }


                handler.post(() -> {
                    textView.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_START);
                    textView.setText(HtmlCompat.fromHtml(stringBuilder.toString(),HtmlCompat.FROM_HTML_MODE_LEGACY));

                });
            }

            @Override
            public void onFailure() {
                handler.post(() -> {
                    Toast.makeText(AboutActivity.this, getResources().getText(R.string.fail_to_get_data),Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

            }

            @Override
            public void onFailure(int code) {
                handler.post(() -> {
                    Toast.makeText(AboutActivity.this, getResources().getText(R.string.fail_to_get_data) + "code:" + code, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
        });
    }

    public void Back(View view) {
        finish();
    }
}