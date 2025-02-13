package com.sjapps.jsonlist;

import static com.sjapps.jsonlist.java.JsonFunctions.*;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sjapps.about.AboutActivity;
import com.sjapps.adapters.ListAdapter;
import com.sjapps.adapters.PathListAdapter;
import com.sjapps.jsonlist.java.JsonData;
import com.sjapps.jsonlist.java.JsonFunctions;
import com.sjapps.jsonlist.java.ListItem;
import com.sjapps.library.customdialog.BasicDialog;
import com.sjapps.library.customdialog.ListDialog;
import com.sjapps.logs.CustomExceptionHandler;
import com.sjapps.logs.LogActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    ImageButton backBtn, menuBtn, splitViewBtn, filterBtn;
    ImageView fileImg;
    Button openFileBtn;
    Button openUrlBtn;
    EditText urlSearch;
    LinearLayout urlLL;
    TextView titleTxt, emptyListTxt, jsonTxt;
    RecyclerView list;
    RecyclerView pathList;
    JsonData data = new JsonData();
    LinearLayout progressView, mainLL;
    LinearProgressIndicator progressBar;
    boolean isMenuOpen, showJson, isRawJsonLoaded, isTopMenuVisible, isUrlSearching, isVertical = true;
    ListAdapter adapter;
    PathListAdapter pathAdapter;
    View menu, dim_bg, pathListView;
    ViewGroup viewGroup;
    AutoTransition autoTransition = new AutoTransition();
    Handler handler = new Handler();
    Thread readFileThread;
    RelativeLayout dropTarget;
    RelativeLayout listRL;
    RelativeLayout rawJsonRL;
    AppState state;
    View fullRawBtn;
    LinearLayout topMenu;
    int listPrevDx = 0;

    ArrayList<String> filterList = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
        checkCrashLogs();
        LoadStateData();
        Log.d(TAG, "onResume: resume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        setContentView(R.layout.activity_main);
        initialize();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            isVertical = false;
            mainLL.setOrientation(LinearLayout.HORIZONTAL);
            updateFullRawBtn();
        }

        functions.setAnimation(this,fileImg,R.anim.scale_in_file_img, new DecelerateInterpolator());
        functions.setAnimation(this,openFileBtn,R.anim.button_pop, new OvershootInterpolator());

        autoTransition.setDuration(150);
        menuBtn.setOnClickListener(view -> open_closeMenu());

        backBtn.setOnClickListener(view -> {
            if(!data.isEmptyPath() || urlLL.getVisibility() == View.VISIBLE) getOnBackPressedDispatcher().onBackPressed();
        });
        openFileBtn.setOnClickListener(view -> ImportFromFile());
        openUrlBtn.setOnClickListener(view -> {
            showUrlSearchView();
        });

        titleTxt.setOnClickListener(v -> {
            if (!data.isEmptyPath())
                showHidePathList();
        });

        pathListView.setOnClickListener(v -> showHidePathList());

        urlSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event != null &&
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                SearchUrl();
                return true;
            }
            return false;
        });

        menu.findViewById(R.id.openFileBtn2).setOnClickListener(view -> {
            ImportFromFile();
            open_closeMenu();
        });
        menu.findViewById(R.id.searchUrlBtn).setOnClickListener(view -> {
            open_closeMenu();
            showUrlSearchView();
        });
        menu.findViewById(R.id.settingsBtn).setOnClickListener(view -> {
            OpenSettings();
            open_closeMenu();
        });
        menu.findViewById(R.id.aboutBtn).setOnClickListener(view -> {
            OpenAbout();
            open_closeMenu();
        });
        menu.findViewById(R.id.logBtn).setOnClickListener(view -> {
            OpenLogPage();
            open_closeMenu();
        });
        dim_bg.setOnClickListener(view -> open_closeMenu());
        splitViewBtn.setOnClickListener(view -> open_closeSplitView());
        filterBtn.setOnClickListener(view -> filter());
        Intent intent = getIntent();
        Log.d(TAG, "onCreate: " + intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            ReadFile(intent.getData());
        }
        if (intent.getAction().equals("android.intent.action.OPEN_FILE")){
            ImportFromFile();
        }

        dropTarget.setOnDragListener((v, event) -> {

            TextView dropTargetTxt = v.findViewById(R.id.dropTargetText);
            View dropTargetBackground = v.findViewById(R.id.dropTargetBackground);

            String MIMEType = state.isMIMEFilterDisabled()?"*/*": Build.VERSION.SDK_INT > Build.VERSION_CODES.P?"application/json":"application/*";

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    dropTarget.setAlpha(1);
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    dropTarget.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                    if(event.getClipDescription().getMimeTypeCount() > 1){
                        dropTargetTxt.setText(R.string.only_one_file_is_allowed);
                        dropTargetBackground.getBackground().mutate().setTint(functions.setColor(this, R.attr.colorError));
                        dropTargetBackground.setAlpha(.8f);
                        return false;
                    }
                    if (!event.getClipDescription().hasMimeType(MIMEType)) {
                        dropTargetTxt.setText(R.string.this_is_not_json_file);
                        dropTargetBackground.getBackground().mutate().setTint(functions.setColor(this, R.attr.colorError));
                        dropTargetBackground.setAlpha(.8f);
                        return false;
                    }

                    dropTargetBackground.getBackground().mutate().setTint(functions.setColor(this, R.attr.colorPrimary));
                    dropTargetBackground.setAlpha(.8f);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    dropTargetTxt.setText(R.string.drop_json_file_here);
                    dropTargetBackground.getBackground().mutate().setTint(functions.setColor(this, R.attr.colorOnBackground));
                    dropTargetBackground.setAlpha(.5f);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    dropTargetTxt.setText(R.string.drop_json_file_here);
                    dropTargetBackground.getBackground().mutate().setTint(functions.setColor(this, R.attr.colorOnBackground));
                    dropTarget.setAlpha(0);
                    return true;

                case DragEvent.ACTION_DROP:
                    if (event.getClipData().getItemCount() > 1){
                        return false;
                    }
                    if (!event.getClipDescription().hasMimeType(MIMEType))
                        return false;
                    if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching) {
                        Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
                        return false;
                    }

                    ClipData.Item item = event.getClipData().getItemAt(0);

                    DragAndDropPermissions dropPermissions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                        dropPermissions = requestDragAndDropPermissions(event);

                    ReadFile(item.getUri());

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && dropPermissions != null)
                        dropPermissions.release();

                    return true;
            }
            return false;
        });
    }

    private void LoadStateData() {
        boolean prevSH = state != null && state.isSyntaxHighlighting();

        state = FileSystem.loadStateData(this);

        if (isRawJsonLoaded && prevSH != state.isSyntaxHighlighting()) {
            isRawJsonLoaded = false;
            if (showJson)
                ShowJSON();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if ((newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) == isVertical){
            return;
        }

        isVertical = !isVertical;

        if (!isVertical){
            mainLL.setOrientation(LinearLayout.HORIZONTAL);
            updateFullRawBtn();
        }else {
            mainLL.setOrientation(LinearLayout.VERTICAL);
            updateFullRawBtn();
        }
    }

    void checkCrashLogs() {

        AppState state = FileSystem.loadStateData(this);
        TextView logBtn = menu.findViewById(R.id.logBtn);
        if (!state.hasCrashLogs()) {
            logBtn.setVisibility(View.GONE);
            return;
        }
        logBtn.setVisibility(View.VISIBLE);

        TypedValue typedValue = new TypedValue();

        if (state.hasNewCrash()) {
            getTheme().resolveAttribute(R.attr.colorOnError, typedValue, true);
            logBtn.setTextColor(typedValue.data);
            logBtn.setBackgroundResource(R.drawable.ripple_red);
            menuBtn.setImageResource(R.drawable.menu_with_dot);
            return;
        }
        getTheme().resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);
        logBtn.setTextColor(typedValue.data);
        logBtn.setBackgroundResource(R.drawable.ripple_list2);
        menuBtn.setImageResource(R.drawable.ic_menu);
    }

    private void OpenSettings() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    private void OpenAbout() {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    private void OpenLogPage() {
        startActivity(new Intent(MainActivity.this, LogActivity.class));
    }

    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (pathListView.getVisibility() == View.VISIBLE){
                showHidePathList();
                return;
            }

            if (isMenuOpen) {
                open_closeMenu();
                return;
            }

            if (urlLL.getVisibility() == View.VISIBLE){
                hideUrlSearchView();
                return;
            }

            if (listRL.getVisibility() == View.GONE){
                FullRaw(null);
                return;
            }

            if (adapter!= null && adapter.selectedItem != -1){
                adapter.selectedItem = -1;
                adapter.notifyItemRangeChanged(0,adapter.getItemCount());
                return;
            }

            if (data.isEmptyPath()){
                BasicDialog dialog = new BasicDialog();
                dialog.Builder(MainActivity.this, true)
                        .setTitle(getString(R.string.exit))
                        .setLeftButtonText(getString(R.string.no))
                        .setRightButtonText(getString(R.string.yes))
                        .onButtonClick(() ->{
                                dialog.dismiss();
                                MainActivity.this.finish();
                        })
                        .show();
                return;
            }
            TransitionManager.endTransitions(viewGroup);
            TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
            data.goBack();
            open(JsonData.getPathFormat(data.getPath()), data.getPath(),-1);
        }
    };

    private void initialize() {
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
        backBtn = findViewById(R.id.backBtn);
        menuBtn = findViewById(R.id.menuBtn);
        mainLL = findViewById(R.id.mainLL);
        splitViewBtn = findViewById(R.id.splitViewBtn);
        filterBtn = findViewById(R.id.filterBtn);
        titleTxt = findViewById(R.id.titleTxt);
        jsonTxt = findViewById(R.id.jsonTxt);
        emptyListTxt = findViewById(R.id.emptyListTxt);
        list = findViewById(R.id.list);
        pathListView = findViewById(R.id.pathListBG);
        pathList = findViewById(R.id.pathList);
        listRL = findViewById(R.id.listRL);
        openFileBtn = findViewById(R.id.openFileBtn);
        openUrlBtn = findViewById(R.id.openUrlBtn);
        urlSearch = findViewById(R.id.urlSearch);
        urlLL = findViewById(R.id.searchUrlView);
        viewGroup = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        dim_bg = findViewById(R.id.dim_layout);
        progressView = findViewById(R.id.loadingView);
        progressBar = findViewById(R.id.progressBar);
        fileImg = findViewById(R.id.fileImg);
        dim_bg.bringToFront();
        menu.bringToFront();
        rawJsonRL = findViewById(R.id.rawJsonRL);
        menuBtn.bringToFront();
        dropTarget = findViewById(R.id.dropTarget);
        fullRawBtn = findViewById(R.id.fullRawBtn);
        topMenu = findViewById(R.id.topMenu);

        LinearLayoutManager pathLM = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public int scrollVerticallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
                int overScroll = dx - scrollRange;

                if ((dx < -40 || overScroll < -10) && !isTopMenuVisible && Math.abs(listPrevDx - dx) < 100) {
                    showTopMenu();
                    listPrevDx = dx;
                    return scrollRange;
                }

                if (dx > 40 && isTopMenuVisible && Math.abs(listPrevDx - dx) < 100){
                    listPrevDx = dx;
                    hideTopMenu();
                }
                listPrevDx = dx;
                return scrollRange;
            }
        };

        list.setLayoutManager(layoutManager);
        pathList.setLayoutManager(pathLM);

    }
    private void showTopMenu() {
        topMenu.animate().cancel();

        isTopMenuVisible = true;
        topMenu.setVisibility(View.VISIBLE);
        topMenu.animate()
                .translationY(0)
                .setDuration(200)
                .start();

    }

    private void hideTopMenu() {
        topMenu.animate().cancel();
      
        isTopMenuVisible = false;
        topMenu.animate()
                .translationY(-topMenu.getHeight())
                .setDuration(100)
                .withEndAction(()-> topMenu.setVisibility(View.GONE))
                .start();
    }

    private void open_closeMenu() {
        if (!isMenuOpen) {
            dim_bg.setVisibility(View.VISIBLE);
            menu.setVisibility(View.VISIBLE);
            menuBtn.setImageResource(R.drawable.ic_close);
            isMenuOpen = true;
        } else {
            dim_bg.setVisibility(View.INVISIBLE);
            menu.setVisibility(View.GONE);
            menuBtn.setImageResource(R.drawable.ic_menu);
            isMenuOpen = false;
        }


    }

    private void showUrlSearchView() {
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching) {
            Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }


        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(View.GONE);
        urlLL.setVisibility(View.VISIBLE);

        if (backBtn.getVisibility() == View.GONE)
            backBtn.setVisibility(View.VISIBLE);

        urlSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(urlSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideUrlSearchView() {
        urlLL.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(View.VISIBLE);
        urlSearch.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(urlSearch.getWindowToken(), 0);

        if (data.isEmptyPath()) {
            backBtn.setVisibility(View.GONE);
        }

    }


    private void LoadData(String Data) {

        loadingStarted(getString(R.string.loading_json));
        emptyListTxt.setVisibility(View.GONE);

        readFileThread = new Thread(() -> {
            ArrayList<ListItem> temp = data.getRootList();
            String tempRaw = data.getRawData();
            JsonElement element;
            try {
                element = JsonParser.parseString(Data);

            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                fileTooLargeException();
                return;
            } catch (Exception e){
                e.printStackTrace();
                fileNotLoadedException();
                return;
            }
            if (element == null) {
                fileNotLoadedException();
                return;
            }
            readFileThread.setName("readFileThread");
            handler.post(()-> loadingStarted(getString(R.string.creating_list)));
            try {
                data.setRootList(null);
                if (element instanceof JsonObject) {
                    Log.d(TAG, "run: Json object");
                    JsonObject object = FileSystem.loadDataToJsonObj(element);
                    data.setRootList(getJsonObject(object));
                }
                if (element instanceof JsonArray) {
                    Log.d(TAG, "run: Json array");
                    JsonArray array = FileSystem.loadDataToJsonArray(element);
                    data.setRootList(getJsonArrayRoot(array));
                }
                if (Data.length()<500000)
                    data.setRawData(Data);
                else data.setRawData("-1");
                data.clearPreviousPos();
            } catch (Exception e){
                e.printStackTrace();
                creatingListException();
                data.setRootList(null);
            }

            if (!data.isRootListNull()) {
                handler.post(() -> {
                    TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

                    if (urlLL.getVisibility() == View.VISIBLE)
                        hideUrlSearchView();

                    data.setCurrentList(data.getRootList());
                    updateFilterList(data.getRootList());
                    adapter = new ListAdapter(data.getRootList(), MainActivity.this, "");
                    pathAdapter = new PathListAdapter(this,data.getPath());
                    list.setAdapter(adapter);
                    pathList.setAdapter(pathAdapter);
                    fileImg.clearAnimation();
                    openFileBtn.clearAnimation();
                    fileImg.setVisibility(View.GONE);
                    openFileBtn.setVisibility(View.GONE);
                    openUrlBtn.setVisibility(View.GONE);
                    functions.setAnimation(MainActivity.this,list,R.anim.scale_in2,new DecelerateInterpolator());
                    list.setVisibility(View.VISIBLE);
                    backBtn.setVisibility(View.GONE);
                    titleTxt.setText("");
                    data.clearPath();
                });

            } else {
                data.setRootList(temp);
                data.setRawData(tempRaw);
                handler.post(() -> loadingFinished(false));
                fileNotLoadedException();
                return;
            }

            isRawJsonLoaded = false;
            if (showJson)
                handler.post(this::ShowJSON);
            else handler.post(() -> loadingFinished(true));

        });
        readFileThread.start();
    }

    public void open(String Title, String path, int previousPosition) {
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        if (isMenuOpen)
            open_closeMenu();

        if (emptyListTxt.getVisibility() == View.VISIBLE)
            emptyListTxt.setVisibility(View.GONE);

        pathAdapter = new PathListAdapter(this,path);
        pathList.setAdapter(pathAdapter);
        data.setPath(path);
        titleTxt.setText(Title);
        ArrayList<ListItem> arrayList = getListFromPath(path,data.getRootList());
        data.setCurrentList(arrayList);
        updateFilterList(arrayList);
        adapter = new ListAdapter(arrayList, this, path);
        list.setAdapter(adapter);

        if (previousPosition == -1) {
            handler.postDelayed(() -> {
                list.smoothScrollToPosition(data.getPreviousPos()+2);
                adapter.setHighlightItem(data.getPreviousPos());
            }, 500);
            handler.postDelayed(() -> {
                adapter.notifyItemChanged(data.getPreviousPos());
            }, 600);
        }
        else data.addPreviousPos(previousPosition);

        if (arrayList.size() == 0) {
            emptyListTxt.setVisibility(View.VISIBLE);
        }
        System.out.println("path = " + path);
        if (!path.isEmpty()) {
            backBtn.setVisibility(View.VISIBLE);
        } else backBtn.setVisibility(View.GONE);

    }

    public void goBack(int n){
        if (pathListView.getVisibility() == View.VISIBLE)
            showHidePathList();
        for (int i = 0; i<n; i++)
            data.goBack();
        open(JsonData.getPathFormat(data.getPath()), data.getPath(),-1);

    }

    private void filter(){
        ListDialog dialog = new ListDialog();
        dialog.Builder(this,true)
                .setTitle(getString(R.string.filter))
                .dialogWithTwoButtons()
                .setSelectableList()
                .setItems(filterList,val -> val)
                .onButtonClick(() -> {
                    setFilter(dialog.getSelectedItems());
                    dialog.dismiss();
                })
                .show();
    }

    public void setFilter(ArrayList<String> items) {
        ArrayList<ListItem> newList = new ArrayList<>();

        if (!items.isEmpty()){
            int pos = -1;
            for (ListItem item : data.getCurrentList()){
                pos++;
                if (item.isSpace() || items.contains(item.getName())){
                    item.setPosition(pos);
                    newList.add(item);
                }
            }
        }else newList = data.getCurrentList();

        adapter = new ListAdapter(newList, this, data.getPath());
        list.setAdapter(adapter);
    }

    private void updateFilterList(ArrayList<ListItem> items) {
        filterList.clear();
        for (ListItem item : items){
            if (!item.isSpace() && item.getName() != null)
                addToFilterList(item.getName());
        }
    }

    public void addToFilterList(String name) {
        if (!filterList.contains(name))
            filterList.add(name);
    }

    private void open_closeSplitView(){
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        if (showJson){
            functions.setAnimation(this,rawJsonRL,isVertical?R.anim.slide_bottom_out:R.anim.slide_right_out,new AccelerateDecelerateInterpolator());
            handler.postDelayed(()-> rawJsonRL.setVisibility(View.GONE),400);
            showJson = false;
            if (listRL.getVisibility() == View.GONE)
                listRL.setVisibility(View.VISIBLE);
            return;
        }
        showJson = true;
        rawJsonRL.setVisibility(View.VISIBLE);
        functions.setAnimation(this,rawJsonRL,isVertical?R.anim.slide_bottom_in:R.anim.slide_right_in,new DecelerateInterpolator());
        if (!isRawJsonLoaded)
            ShowJSON();

    }

    public void showHidePathList() {

        if (pathListView.getVisibility() == View.VISIBLE) {
            pathListView.setVisibility(View.GONE);
            return;
        }

        pathListView.setVisibility(View.VISIBLE);
    }

    public void FullRaw(View view) {
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        fullRawBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        if (listRL.getVisibility() == View.VISIBLE){
            listRL.setVisibility(View.GONE);
        }else
            listRL.setVisibility(View.VISIBLE);
    }

    private void updateFullRawBtn(){

        int initWidth = functions.dpToPixels(this,100);
        int initHeight = functions.dpToPixels(this,7);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fullRawBtn.getLayoutParams();

        if (!isVertical){

            params.width = initHeight;
            params.height = initWidth;

            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.removeRule(RelativeLayout.CENTER_HORIZONTAL);

            fullRawBtn.setLayoutParams(params);
            return;
        }

        params.width = initWidth;
        params.height = initHeight;

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.removeRule(RelativeLayout.CENTER_VERTICAL);

        fullRawBtn.setLayoutParams(params);
    }

    private void ShowJSON(){

        if (data.getRawData().equals("-1")) {
            Snackbar.make(getWindow().getDecorView(), R.string.file_is_to_large_to_be_shown_in_a_split_screen, BaseTransientBottomBar.LENGTH_SHORT).show();
            if (progressView.getVisibility() == View.VISIBLE)
                loadingFinished(true);
            if (showJson)
                open_closeSplitView();
            return;
        }
        if (data.getRawData().equals(""))
            return;

        loadingStarted(getString(R.string.displaying_json));

        Thread thread = new Thread(() -> {
            String dataStr = JsonFunctions.getAsPrettyPrint(data.getRawData());
            handler.post(()-> {
                if (state.isSyntaxHighlighting()) {
                    SpannableStringBuilder builder = highlightJsonSyntax(dataStr);
                    jsonTxt.setText(builder);
                }else jsonTxt.setText(dataStr);
                loadingFinished(true);
                isRawJsonLoaded = true;
            });
        });
        thread.setName("loadingJson");
        thread.start();

    }

    private SpannableStringBuilder highlightJsonSyntax(String json) {

        SpannableStringBuilder spannable = new SpannableStringBuilder(json);

        int keyColor = functions.setColor(this,R.attr.colorPrimary);
        int numberColor = functions.setColor(this,R.attr.colorTertiary);
        int booleanAndNullColor = functions.setColor(this,R.attr.colorError);

        Pattern keyPattern = Pattern.compile("(\"\\w+\")\\s*:");
        Pattern numberPattern = Pattern.compile(":\\s(-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?)");
        Pattern booleanAndNullPattern = Pattern.compile(":\\s*(true|false|null)");

        Pattern[] patterns = {keyPattern, numberPattern, booleanAndNullPattern};
        int[] colors = {keyColor, numberColor, booleanAndNullColor};

        for (int i = 0; i < patterns.length; i++) {
            applyPatternHighlighting(spannable, json, patterns[i], colors[i]);
        }

        return spannable;
    }

    private void applyPatternHighlighting(SpannableStringBuilder spannable, String json, Pattern pattern, int color) {
        Matcher matcher = pattern.matcher(json);
        while (matcher.find()) {
            spannable.setSpan(
                    new ForegroundColorSpan(color),
                    matcher.start(1),
                    matcher.end(1),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }

    private void ImportFromFile() {
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching) {
            Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(state.isMIMEFilterDisabled()?"*/*" : Build.VERSION.SDK_INT > Build.VERSION_CODES.P?"application/json":"application/*");
        ActivityResultData.launch(intent);
    }


    ActivityResultLauncher<Intent> ActivityResultData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    if(result.getResultCode() == Activity.RESULT_CANCELED){
                        Toast.makeText(MainActivity.this, R.string.import_data_canceled,Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (result.getData() == null || result.getData().getData() == null){
                    Toast.makeText(MainActivity.this, R.string.fail_to_load_data, Toast.LENGTH_SHORT).show();
                    return;
                }
                //File
                ReadFile(result.getData().getData());
            });

    void ReadFile(Uri uri){
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching){
            return;
        }
        loadingStarted(getString(R.string.reading_file));

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            AssetFileDescriptor fileDescriptor = getContentResolver().openAssetFileDescriptor(uri , "r");

            readFileThread = new Thread(() -> {

                String Data = FileSystem.LoadDataFromFile(MainActivity.this, uri, inputStream, fileDescriptor);

                if (Data == null) {
                    Log.d(TAG, "ReadFile: null data");
                    return;
                }
                handler.post(() -> {
                    LoadData(Data);
                });

            });
            readFileThread.setName("readFileThread");
            readFileThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void SearchUrl(View view) {
        SearchUrl();
    }

    private void SearchUrl() {
        getFromUrl(urlSearch.getText().toString());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlSearch.getWindowToken(), 0);
    }

    void getFromUrl(String url){
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
            Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
            return;
        }

        hideUrlSearchView();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                handler.post(()-> loadingFinished(false));
                isUrlSearching = false;
                handler.post(()-> Toast.makeText(MainActivity.this,"Fail",Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                handler.post(()-> loadingFinished(false));
                isUrlSearching = false;
                if (response.body() != null)
                    LoadData(response.body().string());
                else handler.post(()->Toast.makeText(MainActivity.this, "Fail, Code:" + response.code(), Toast.LENGTH_SHORT).show());
            }
        });
        loadingStarted();
        isUrlSearching = true;

    }

    void loadingStarted(){
        loadingStarted(getString(R.string.loading));

    }

    void loadingStarted(String txt){
        TextView text =  progressView.findViewById(R.id.loadingTxt);
        progressBar.setIndeterminate(true);
        text.setText(txt);
        handler.postDelayed(() -> {
            if (progressView.getVisibility() != View.VISIBLE) {
                functions.setAnimation(this, progressView, R.anim.scale_in);
                text.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.VISIBLE);
            }
        },300);

    }
    public void updateProgress(int val){
        handler.post(()->{
            progressBar.setProgressCompat(val,true);
        });

    }

    void loadingFinished(boolean isFinished){

        if (!isFinished){
            handler.postDelayed(()-> {
                functions.setAnimation(this, progressView,R.anim.scale_out);
                progressView.setVisibility(View.INVISIBLE);
            },300);
            return;
        }

        progressBar.setIndeterminate(false);
        progressBar.setProgressCompat(100,true);

        TextView text =  progressView.findViewById(R.id.loadingTxt);
        handler.postDelayed(() -> text.setText( R.string.finished),500);
        handler.postDelayed(() -> {
        },700);
        handler.postDelayed(() -> text.setVisibility(View.INVISIBLE),900);
        handler.postDelayed(() -> {
            functions.setAnimation(this, progressView,R.anim.scale_out);
            progressView.setVisibility(View.INVISIBLE);
        },1000);
    }

    void fileTooLargeException(){
        postMessageException(getString(R.string.file_is_too_large));
    }
    void fileNotLoadedException(){
        postMessageException(getString(R.string.fail_to_load_file));
    }
    void creatingListException(){
        postMessageException(getString(R.string.fail_to_create_list));
    }
    void postMessageException(String msg){
        handler.post(() -> {
            Toast.makeText(MainActivity.this,msg, Toast.LENGTH_SHORT).show();
            loadingFinished(false);
        });
    }
}