package com.sjapps.jsonlist;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.sj14apps.jsonlist.core.JsonFunctions.*;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingtoolbar.FloatingToolbarLayout;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import com.sj14apps.jsonlist.core.JsonFunctions;
import com.sj14apps.jsonlist.core.SearchItem;
import com.sjapps.about.AboutActivity;
import com.sjapps.adapters.ListAdapter;
import com.sjapps.adapters.PathListAdapter;
import com.sjapps.adapters.SearchListAdapter;
import com.sjapps.jsonlist.controllers.AndroidDragAndDrop;
import com.sjapps.jsonlist.controllers.AndroidFileManager;
import com.sjapps.jsonlist.controllers.AndroidJsonLoader;
import com.sjapps.jsonlist.controllers.AndroidRawJsonView;
import com.sj14apps.jsonlist.core.controllers.FileManager;
import com.sj14apps.jsonlist.core.controllers.JsonLoader;
import com.sj14apps.jsonlist.core.controllers.RawJsonView;
import com.sj14apps.jsonlist.core.controllers.WebManager;
import com.sj14apps.jsonlist.core.AppState;
import com.sj14apps.jsonlist.core.JsonData;
import com.sj14apps.jsonlist.core.ListItem;
import com.sjapps.library.customdialog.BasicDialog;
import com.sjapps.library.customdialog.CustomViewDialog;
import com.sjapps.library.customdialog.DialogButtonEvents;
import com.sjapps.library.customdialog.ListDialog;
import com.sjapps.library.customdialog.MessageDialog;
import com.sjapps.logs.CustomExceptionHandler;
import com.sjapps.logs.LogActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    ImageButton backBtn, menuBtn, splitViewBtn, filterBtn, searchBtn;
    View editBtn;
    ImageView fileImg;
    Button openFileBtn;
    Button openUrlBtn;
    EditText urlSearchTxt,searchTxt;
    LinearLayout urlLL;
    LinearLayout searchLL;
    LinearLayout messageLL;
    TextView titleTxt, emptyListTxt;
    RecyclerView list;
    RecyclerView pathList;
    RecyclerView searchList;
    public JsonData data = new JsonData();
    public LinearLayout progressView;
    ConstraintLayout mainLL;
    LinearProgressIndicator progressBar;
    ListAdapter adapter;
    PathListAdapter pathAdapter;
    SearchListAdapter searchAdapter;
    View menu, dim_bg, pathListView;
    public WebView rawJsonWV;
    public ViewGroup viewGroup;
    public AutoTransition autoTransition = new AutoTransition();
    public Handler handler = new Handler();
    public Thread readFileThread;
    public RelativeLayout listRL;
    public RelativeLayout rawJsonRL;
    public AppState state;
    public View resizeSplitViewBtn;
    FloatingToolbarLayout toolbar;
    int listPrevDx = 0;
    RawJsonView rawJsonView;
    FileManager fileManager;
    WebManager webManager;
    JsonLoader jsonLoader;

    public boolean isVertical = true;
    public boolean isUrlSearching;
    boolean isMenuOpen;
    boolean isTopMenuVisible;
    boolean isEdited;
    public boolean isEditMode;
    boolean unsavedChanges;
    ImageButton saveBtn;
    public Guideline guideLine;

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
        setLayoutBounds();
        setEvents();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            isVertical = false;
            updateOrientation();
        }

        Intent intent = getIntent();
        Log.d(TAG, "onCreate: " + intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            ReadFile(intent.getData());
        }
        if (intent.getAction().equals("android.intent.action.OPEN_FILE")){
            fileManager.importFromFile();
        }
        if (intent.getAction().equals("android.intent.action.OPEN_URL")){
            showUrlSearchView();
        }

        functions.setAnimation(this,fileImg,R.anim.scale_in_file_img, new DecelerateInterpolator());
        functions.setAnimation(this,openFileBtn,R.anim.button_pop, new OvershootInterpolator());

        autoTransition.setDuration(150);
    }

    private void initialize() {
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
        backBtn = findViewById(R.id.backBtn);
        menuBtn = findViewById(R.id.menuBtn);
        mainLL = findViewById(R.id.mainLL);
        messageLL = findViewById(R.id.messageLL);
        splitViewBtn = findViewById(R.id.splitViewBtn);
        filterBtn = findViewById(R.id.filterBtn);
        searchBtn = findViewById(R.id.searchBtn);
        editBtn = findViewById(R.id.editBtn);
        titleTxt = findViewById(R.id.titleTxt);
        emptyListTxt = findViewById(R.id.emptyListTxt);
        list = findViewById(R.id.list);
        pathListView = findViewById(R.id.pathListBG);
        pathList = findViewById(R.id.pathList);
        searchList = findViewById(R.id.searchResultList);
        listRL = findViewById(R.id.listRL);
        openFileBtn = findViewById(R.id.openFileBtn);
        openUrlBtn = findViewById(R.id.openUrlBtn);
        urlSearchTxt = findViewById(R.id.urlSearch);
        searchTxt = findViewById(R.id.searchTxt);
        urlLL = findViewById(R.id.searchUrlView);
        searchLL = findViewById(R.id.searchLL);
        viewGroup = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        dim_bg = findViewById(R.id.dim_layout);
        progressView = findViewById(R.id.loadingView);
        progressBar = findViewById(R.id.progressBar);
        fileImg = findViewById(R.id.fileImg);
        dim_bg.bringToFront();
        menu.bringToFront();
        rawJsonRL = findViewById(R.id.rawJsonRL);
        rawJsonWV = findViewById(R.id.rawJsonWV);
        menuBtn.bringToFront();
        resizeSplitViewBtn = findViewById(R.id.resizeSplitViewBtn);
        toolbar = findViewById(R.id.floating_toolbar);
        saveBtn = findViewById(R.id.saveBtn);
        guideLine = findViewById(R.id.guideline);

        LinearLayoutManager pathLM = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public int scrollVerticallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
                int overScroll = dx - scrollRange;

                if ((dx < -40 || overScroll < -10) && !isTopMenuVisible && Math.abs(listPrevDx - dx) < 100) {
                    showToolbar();
                    listPrevDx = dx;
                    return scrollRange;
                }

                if (dx > 40 && isTopMenuVisible && Math.abs(listPrevDx - dx) < 100){
                    listPrevDx = dx;
                    hideToolbar();
                }
                listPrevDx = dx;
                return scrollRange;
            }
        };

        int textColor = functions.setColor(this,R.attr.colorOnSecondaryContainer);
        int keyColor = functions.setColor(this,R.attr.colorPrimary);
        int numberColor = functions.setColor(this,R.attr.colorTertiary);
        int booleanAndNullColor = functions.setColor(this,R.attr.colorError);
        int bgColor = functions.setColor(this,R.attr.colorSecondaryContainer);

        rawJsonView = new AndroidRawJsonView(this,textColor,keyColor,numberColor,booleanAndNullColor,bgColor);

        webManager =  new WebManager();

        rawJsonView.updateRawJson("");

        list.setLayoutManager(layoutManager);
        pathList.setLayoutManager(pathLM);
        searchList.setLayoutManager(new LinearLayoutManager(this));

        fileManager = new AndroidFileManager(this,handler);
        jsonLoader = new AndroidJsonLoader(this);

        new AndroidDragAndDrop(this, dragAndDropCallback);

    }

    private void setLayoutBounds() {
        ViewCompat.setOnApplyWindowInsetsListener(viewGroup, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets insetsN = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            layoutParams.leftMargin = insets.left + insetsN.left;
            layoutParams.topMargin = insets.top;
            layoutParams.rightMargin = insets.right + insetsN.right;
            layoutParams.bottomMargin = insets.bottom;
            v.setLayoutParams(layoutParams);

            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            searchLL.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeInsets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setEvents(){
        menuBtn.setOnClickListener(view -> open_closeMenu());

        backBtn.setOnClickListener(view -> {
            if(!data.isEmptyPath() || urlLL.getVisibility() == VISIBLE || searchLL.getVisibility() == VISIBLE || (adapter != null && adapter.isEditMode())) getOnBackPressedDispatcher().onBackPressed();
        });
        openFileBtn.setOnClickListener(view -> fileManager.importFromFile());
        openUrlBtn.setOnClickListener(view -> {
            showUrlSearchView();
        });

        titleTxt.setOnClickListener(v -> {
            if (!data.isEmptyPath())
                showHidePathList();
        });

        pathListView.setOnClickListener(v -> showHidePathList());

        //TODO Web
        urlSearchTxt.setOnEditorActionListener((v, actionId, event) -> {
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


        ChipGroup searchChipGroup = findViewById(R.id.searchChipGroup);
        searchChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()){
                boolean chip1 = ((Chip) group.getChildAt(0)).isChecked();
                boolean chip2 = ((Chip) group.getChildAt(1)).isChecked();

                if (chip1 && chip2)
                    data.searchMode = 0;
                else if (chip1)
                    data.searchMode = 1;
                else if (chip2)
                    data.searchMode = 2;

            }else data.searchMode = 0;

            if (searchLL.getVisibility() == VISIBLE)
                search(searchTxt.getText().toString());
        });

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchLL.getVisibility() == VISIBLE)
                    search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        menu.findViewById(R.id.openFileBtn2).setOnClickListener(view -> {
            fileManager.importFromFile();
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
        splitViewBtn.setOnClickListener(view -> rawJsonView.toggleSplitView());
        filterBtn.setOnClickListener(view -> filter());
        searchBtn.setOnClickListener(view -> showSearchView());
        editBtn.setOnClickListener(view -> toggleEdit());
        saveBtn.setOnClickListener(view -> saveChanges());

        resizeSplitViewBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        resizeSplitViewBtn.setScaleX(1.2f);
                        resizeSplitViewBtn.setScaleY(1.2f);
                        resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
                        if (isVertical){
                            params.guidePercent = (event.getRawY() - mainLL.getY()) / viewGroup.getHeight();
                        }else {
                            params.guidePercent = (event.getRawX() - mainLL.getX()) / viewGroup.getWidth();
                        }
                        if (params.guidePercent < 0.2f){
                            params.guidePercent = 0.03f;
                            if (listRL.getVisibility() == VISIBLE)
                                resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                            listRL.setVisibility(GONE);
                        }else {
                            if (listRL.getVisibility() == GONE)
                                resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                            listRL.setVisibility(VISIBLE);
                        }
                        if (params.guidePercent > 0.8f)
                            params.guidePercent = 0.8f;
                        guideLine.setLayoutParams(params);
                        return true;

                    case MotionEvent.ACTION_UP:

                        resizeSplitViewBtn.setScaleX(1f);
                        resizeSplitViewBtn.setScaleY(1f);
                        resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        return true;
                }
                return true;
            }
        });
    }

    private void search(String string) {

        if (searchAdapter == null){
            searchAdapter = new SearchListAdapter(this, new ArrayList<>());
        }

        ArrayList<SearchItem> searchItems;
        if (string.isEmpty())
            searchItems = new ArrayList<>();
        else searchItems = JsonFunctions.searchItem(data,string);

        searchAdapter.setSearchItems(searchItems);
        searchAdapter.notifyDataSetChanged();

    }

    private void toggleEdit() {
        if (adapter == null)
            return;

        isEditMode = !adapter.isEditMode();

        if (isEditMode){
            showBackBtn();
            hideToolbar();
            menuBtn.setVisibility(INVISIBLE);
            splitViewBtn.setVisibility(INVISIBLE);
        }
        else {
            menuBtn.setVisibility(VISIBLE);
            splitViewBtn.setVisibility(VISIBLE);
            hideBackBtnIfNotNeeded();

            if (isEdited){
                data.setRawData(JsonFunctions.convertToRawString(data.getRootList()));
                isEdited = false;
                rawJsonView.isRawJsonLoaded = false;
                unsavedChanges = true;
                saveBtn.setVisibility(VISIBLE);
                if (rawJsonView.showJson){
                    rawJsonView.ShowJSON();
                }
            }
        }

        adapter.setEditMode(isEditMode);
        adapter.notifyItemRangeChanged(0,adapter.getItemCount());
        messageLL.setVisibility(isEditMode ? VISIBLE: GONE);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if ((newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) == isVertical){
            return;
        }

        isVertical = !isVertical;
        updateOrientation();
    }

    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (pathListView.getVisibility() == VISIBLE){
                showHidePathList();
                return;
            }

            if (isMenuOpen) {
                open_closeMenu();
                return;
            }

            if (isEditMode){
                toggleEdit();
                return;
            }

            if (urlLL.getVisibility() == VISIBLE){
                hideUrlSearchView();
                return;
            }
            if (searchLL.getVisibility() == VISIBLE){
                hideSearchView();
                return;
            }

            if (listRL.getVisibility() == GONE){
                ShowList();
                return;
            }

            if (adapter!= null && adapter.selectedItem != -1){
                adapter.selectedItem = -1;
                adapter.notifyItemRangeChanged(0,adapter.getItemCount());
                return;
            }

            if (data.isEmptyPath()){

                if (unsavedChanges){
                    BasicDialog dialog = new BasicDialog();
                    dialog.Builder(MainActivity.this, true)
                            .setTitle(getString(R.string.save_changes))
                            .setMessage(getString(R.string.unsaved_changes_msg))
                            .setLeftButtonText(getString(R.string.dismiss))
                            .setRightButtonText(getString(R.string.save))

                            .onButtonClick(new DialogButtonEvents() {
                                @Override
                                public void onLeftButtonClick() {
                                    dialog.dismiss();
                                    MainActivity.this.finish();
                                }

                                @Override
                                public void onRightButtonClick() {
                                    dialog.dismiss();
                                    saveChanges();
                                }
                            })
                            .show();
                    return;
                }

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

    private void saveChanges() {
        fileManager.saveFile(data.getFileName());
    }


    //TODO FileManager??
    public void LoadStateData() {
        boolean prevSH = state != null && state.isSyntaxHighlighting();

        state = FileSystem.loadStateData(this);

        if (rawJsonView.isRawJsonLoaded && prevSH != state.isSyntaxHighlighting()) {
            rawJsonView.isRawJsonLoaded = false;
            if (rawJsonView.showJson)
                rawJsonView.ShowJSON();
        }
    }

    void checkCrashLogs() {

        AppState state = FileSystem.loadStateData(this);
        TextView logBtn = menu.findViewById(R.id.logBtn);
        if (!state.hasCrashLogs()) {
            logBtn.setVisibility(GONE);
            return;
        }
        logBtn.setVisibility(VISIBLE);

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

    private void showToolbar() {
        if (isEditMode)
            return;

        toolbar.animate().cancel();

        isTopMenuVisible = true;
        toolbar.setVisibility(VISIBLE);
        toolbar.animate()
                .translationY(0)
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .setDuration(500)
                .start();

    }

    private void hideToolbar() {
        toolbar.animate().cancel();

        isTopMenuVisible = false;
        toolbar.animate()
                .translationY(toolbar.getHeight()+50)
                .setDuration(300)
                .scaleX(.5f)
                .scaleY(.5f)
                .alpha(0)
                .withEndAction(()-> toolbar.setVisibility(GONE))
                .start();
    }

    private void open_closeMenu() {
        if (!isMenuOpen) {
            dim_bg.setVisibility(VISIBLE);
            menu.setVisibility(VISIBLE);
            menuBtn.setImageResource(R.drawable.ic_close);
            isMenuOpen = true;
        } else {
            dim_bg.setVisibility(INVISIBLE);
            menu.setVisibility(GONE);
            menuBtn.setImageResource(R.drawable.ic_menu);
            isMenuOpen = false;
        }
    }

    private void showUrlSearchView() {
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching) {
            Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

        if (searchLL.getVisibility() == VISIBLE){
            hideSearchView();
        }


        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(GONE);
        urlLL.setVisibility(VISIBLE);

        if (backBtn.getVisibility() == GONE)
            backBtn.setVisibility(VISIBLE);

        urlSearchTxt.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(urlSearchTxt, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideUrlSearchView() {
        urlLL.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(VISIBLE);
        urlSearchTxt.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(urlSearchTxt.getWindowToken(), 0);

        if (data.isEmptyPath()) {
            backBtn.setVisibility(GONE);
        }

    }

    private void showSearchView() {
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(GONE);
        searchLL.setVisibility(VISIBLE);

        if (backBtn.getVisibility() == GONE)
            backBtn.setVisibility(VISIBLE);

        searchTxt.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchTxt, InputMethodManager.SHOW_IMPLICIT);

        searchAdapter = new SearchListAdapter(this,new ArrayList<>());

        searchList.setAdapter(searchAdapter);

    }

    public void hideSearchView() {
        searchLL.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(VISIBLE);
        searchTxt.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);

        if (data.isEmptyPath()) {
            backBtn.setVisibility(GONE);
        }

    }

    public void open(String Title, String path, int previousPosition) {
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        if (isMenuOpen)
            open_closeMenu();

        if (emptyListTxt.getVisibility() == VISIBLE)
            emptyListTxt.setVisibility(GONE);



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

        if (arrayList.isEmpty()) {
            emptyListTxt.setVisibility(VISIBLE);
        }
        System.out.println("path = " + path);
        if (!path.isEmpty()) {
            backBtn.setVisibility(VISIBLE);
        } else backBtn.setVisibility(GONE);

    }

    public void highlightItem(int id){
        handler.postDelayed(() -> {
            list.smoothScrollToPosition(id+2);
            adapter.setHighlightItem(id);
        }, 500);
        handler.postDelayed(() -> {
            adapter.notifyItemChanged(id);
        }, 600);
    }

    public void goBack(int n){
        if (pathListView.getVisibility() == VISIBLE)
            showHidePathList();
        for (int i = 0; i<n; i++)
            data.goBack();
        open(JsonData.getPathFormat(data.getPath()), data.getPath(),-1);

    }

    private void showBackBtn() {
        if (backBtn.getVisibility() == VISIBLE)
            return;
        backBtn.setVisibility(VISIBLE);
    }

    private void hideBackBtnIfNotNeeded() {
        if (data.isEmptyPath())
            backBtn.setVisibility(GONE);
    }

    private void filter() {
        ListDialog dialog = new ListDialog();
        dialog.Builder(this, true)
                .setTitle(getString(R.string.filter))
                .dialogWithTwoButtons()
                .setSelectableList()
                .setItems(filterList, val -> val)
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

    public void showHidePathList() {

        if (isEditMode)
            return;

        if (pathListView.getVisibility() == VISIBLE) {
            pathListView.setVisibility(GONE);
            return;
        }

        pathListView.setVisibility(VISIBLE);
    }

    public void ShowList() {
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        listRL.setVisibility(VISIBLE);
        guideLine.setGuidelinePercent(.5f);
    }

    private void updateOrientation(){

        int initWidth = functions.dpToPixels(this,100);
        int initHeight = functions.dpToPixels(this,7);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) resizeSplitViewBtn.getLayoutParams();
        FrameLayout.LayoutParams paramsLine = (FrameLayout.LayoutParams) findViewById(R.id.resizeSplitViewBtnLine).getLayoutParams();

        mainLL.removeView(guideLine);
        guideLine = new Guideline(this);
        guideLine.setId(R.id.guideline);

        ConstraintLayout.LayoutParams paramsGuideLine =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT);

        if (rawJsonView.showJson)
            paramsGuideLine.guidePercent = 0.5f;
        else paramsGuideLine.guidePercent = 1;

        ConstraintLayout constraintLayout = findViewById(R.id.mainLL);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);

        constraintSet.clear(R.id.resizeSplitViewBtn);
        constraintSet.clear(R.id.listRL);
        constraintSet.clear(R.id.rawJsonRL);

        if (!isVertical){
            paramsGuideLine.orientation = ConstraintLayout.LayoutParams.VERTICAL;
            mainLL.addView(guideLine,1,paramsGuideLine);

            paramsLine.width = initHeight;
            paramsLine.height = initWidth;

            params.leftMargin = functions.dpToPixels(this,-15);
            params.topMargin = 0;
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = rawJsonRL.getId();
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

            constraintSet.connect(R.id.listRL,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
            constraintSet.connect(R.id.listRL,ConstraintSet.END,R.id.guideline,ConstraintSet.START);
            constraintSet.connect(R.id.listRL,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
            constraintSet.connect(R.id.listRL,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);

            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.START,R.id.guideline,ConstraintSet.START);
            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);

            constraintSet.applyTo(constraintLayout);
            return;
        }

        paramsGuideLine.orientation = ConstraintLayout.LayoutParams.HORIZONTAL;
        mainLL.addView(guideLine,paramsGuideLine);

        paramsLine.width = initWidth;
        paramsLine.height = initHeight;

        params.leftMargin = 0;
        params.topMargin = functions.dpToPixels(this,-15);
        params.topToTop = rawJsonRL.getId();
        params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        constraintSet.connect(R.id.listRL,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
        constraintSet.connect(R.id.listRL,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
        constraintSet.connect(R.id.listRL,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
        constraintSet.connect(R.id.listRL,ConstraintSet.BOTTOM,R.id.guideline,ConstraintSet.BOTTOM);

        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.TOP,R.id.guideline,ConstraintSet.TOP);
        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);

        constraintSet.applyTo(constraintLayout);
    }

    void ReadFile(Uri uri){
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching){
            return;
        }
        ((AndroidFileManager) fileManager).validatePath(uri);

        loadingStarted(getString(R.string.reading_file));

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            AssetFileDescriptor fileDescriptor = getContentResolver().openAssetFileDescriptor(uri, "r");
            if (fileDescriptor == null) {
                fileCallback.onFileLoadFailed();
                return;
            }

            String fileName = AndroidFileManager.getFileName(this,uri);
            long fileSize = fileDescriptor.getLength();

            fileDescriptor.close();
            readFileThread = new Thread(() -> {
                fileManager.readFile(inputStream, fileName , fileSize, fileCallback);
            });
            readFileThread.setName("readFileThread");
            readFileThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void WriteFile(Uri uri){
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching){
            return;
        }
        loadingStarted(getString(R.string.saving_file));

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);

            readFileThread = new Thread(() -> {
                fileManager.writeFile(outputStream, data.getRawData(), fileWriteCallback);
            });
            readFileThread.setName("writeFileThread");
            readFileThread.start();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void SearchUrl(View view) {
        SearchUrl();
    }

    private void SearchUrl() {
        webManager.getFromUrl(urlSearchTxt.getText().toString(),webCallback);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlSearchTxt.getWindowToken(), 0);
    }

    public void loadingStarted(){
        loadingStarted(getString(R.string.loading));

    }

    public void loadingStarted(String txt){
        TextView text =  progressView.findViewById(R.id.loadingTxt);
        progressBar.setIndeterminate(true);
        text.setText(txt);
        handler.postDelayed(() -> {
            if (progressView.getVisibility() != VISIBLE) {
                functions.setAnimation(this, progressView, R.anim.scale_in);
                text.setVisibility(VISIBLE);
                progressView.setVisibility(VISIBLE);
            }
        },300);

    }

    public void loadingFinished(boolean isFinished){

        if (!isFinished){
            handler.postDelayed(()-> {
                functions.setAnimation(this, progressView,R.anim.scale_out);
                progressView.setVisibility(INVISIBLE);
            },300);
            return;
        }

        progressBar.setIndeterminate(false);
        progressBar.setProgressCompat(100,true);

        TextView text =  progressView.findViewById(R.id.loadingTxt);
        handler.postDelayed(() -> text.setText( R.string.finished),500);
        handler.postDelayed(() -> {
        },700);
        handler.postDelayed(() -> text.setVisibility(INVISIBLE),900);
        handler.postDelayed(() -> {
            functions.setAnimation(this, progressView,R.anim.scale_out);
            progressView.setVisibility(INVISIBLE);
        },1000);
    }


    private final FileManager.FileCallback fileCallback = new FileManager.FileCallback() {
        @Override
        public void onFileLoaded(String data, String fileName) {
            if (data == null) {
                Log.d(TAG, "ReadFile: null data");
                return;
            }
            handler.post(() -> {
                jsonLoader.LoadData(data, fileName, jsonLoaderCallback);
            });

        }

        @Override
        public void onFileLoadFailed() {
            Toast.makeText(MainActivity.this, R.string.fail_to_load_file, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgressUpdate(int progress) {
            handler.post(()->{
                progressBar.setProgressCompat(progress,true);
            });
        }
    };

    private final FileManager.FileWriteCallback fileWriteCallback = new FileManager.FileWriteCallback() {
        @Override
        public void onFileWriteSuccess() {
            unsavedChanges = false;
            loadingFinished(true);
            saveBtn.setVisibility(GONE);
        }

        @Override
        public void onFileWriteFail() {
            loadingFinished(false);
            Toast.makeText(MainActivity.this, getString(R.string.fail_to_save), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgressUpdate(int progress) {
            progressBar.setProgressCompat(progress,true);
        }
    };

    JsonLoader.JsonLoaderCallback jsonLoaderCallback = new JsonLoader.JsonLoaderCallback() {
        @Override
        public void start() {
            loadingStarted(getString(R.string.loading_json));
            emptyListTxt.setVisibility(GONE);
        }

        @Override
        public void started() {
            handler.post(()-> loadingStarted(getString(R.string.creating_list)));
        }

        @Override
        public void failed() {
            handler.post(() -> loadingFinished(false));
        }

        @Override
        public void success() {
            handler.post(() -> {
                TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

                if (urlLL.getVisibility() == VISIBLE)
                    hideUrlSearchView();

                if (searchLL.getVisibility() == VISIBLE)
                    hideSearchView();

                data.setCurrentList(data.getRootList());
                updateFilterList(data.getRootList());
                adapter = new ListAdapter(data.getRootList(), MainActivity.this, "");
                pathAdapter = new PathListAdapter(MainActivity.this,data.getPath());
                list.setAdapter(adapter);
                pathList.setAdapter(pathAdapter);
                fileImg.clearAnimation();
                openFileBtn.clearAnimation();
                fileImg.setVisibility(GONE);
                openFileBtn.setVisibility(GONE);
                openUrlBtn.setVisibility(GONE);
                functions.setAnimation(MainActivity.this,list,R.anim.scale_in2,new DecelerateInterpolator());
                list.setVisibility(VISIBLE);
                backBtn.setVisibility(GONE);
                saveBtn.setVisibility(GONE);
                unsavedChanges = false;
                titleTxt.setText("");
                data.clearPath();

                if (!isTopMenuVisible)
                    showToolbar();
            });
        }

        @Override
        public void after() {
            rawJsonView.isRawJsonLoaded = false;
            if (rawJsonView.showJson)
                handler.post(() -> rawJsonView.ShowJSON());
            else handler.post(() -> loadingFinished(true));
        }
    };

    private final WebManager.WebCallback webCallback = new WebManager.WebCallback() {
        @Override
        public void onStarted() {
            hideUrlSearchView();
            loadingStarted();
            isUrlSearching = true;
        }

        @Override
        public void onInvalidURL() {
            Toast.makeText(MainActivity.this, getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(String data) {
            handler.post(()-> loadingFinished(false));
            isUrlSearching = false;
            jsonLoader.LoadData(data,null,jsonLoaderCallback);
        }

        @Override
        public void onFailure() {
            handler.post(()-> loadingFinished(false));
            isUrlSearching = false;
            handler.post(()-> Toast.makeText(MainActivity.this,"Fail",Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onFailure(int code) {
            handler.post(()-> loadingFinished(false));
            isUrlSearching = false;
            handler.post(()->Toast.makeText(MainActivity.this, "Fail, Code:" + code, Toast.LENGTH_SHORT).show());
        }
    };

    private final AndroidDragAndDrop.DragAndDropCallback dragAndDropCallback = new AndroidDragAndDrop.DragAndDropCallback() {
        @Override
        public boolean checkIfFileIsLoading() {
            if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching) {
                Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
                return true;
            }
            if (isEditMode){
                Snackbar.make(getWindow().getDecorView(), R.string.editing_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
                return true;
            }

            return false;
        }

        @Override
        public void onDrop(Uri uri) {
            ReadFile(uri);
        }
    };

    public ActivityResultLauncher<Intent> ActivityResultData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    if(result.getResultCode() == Activity.RESULT_CANCELED){
                        Toast.makeText(MainActivity.this, R.string.import_data_canceled,Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (result.getData() == null || result.getData().getData() == null){
                    fileCallback.onFileLoadFailed();
                    return;
                }
                //File
                ReadFile(result.getData().getData());
            });

    public ActivityResultLauncher<Intent> ActivityResultSaveData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    return;
                }
                if (result.getData() == null || result.getData().getData() == null){
                    fileWriteCallback.onFileWriteFail();
                    return;
                }
                WriteFile(result.getData().getData());
            });

    public void editItem(int pos) {
        View view = LayoutInflater.from(this).inflate(R.layout.edit_item,null);

        ListItem item = adapter.getList().get(pos);

        if (item.isRootItem()) {
            MessageDialog dialog = new MessageDialog();
            dialog.Builder(this,true)
                    .setTitle(getString(R.string.editing_root_item_not_available))
                    .show();
            return;
        }

        EditText nameTxt = view.findViewById(R.id.NameTxt);
        EditText valueTxt = view.findViewById(R.id.ValueTxt);

        if (item.getName() == null) {
            view.findViewById(R.id.nameLL).setVisibility(GONE);
        }else nameTxt.setText(adapter.getList().get(pos).getName());

        if (item.isArray() || item.isObject()) {
            view.findViewById(R.id.valueLL).setVisibility(GONE);
        }else valueTxt.setText(adapter.getList().get(pos).getValue());

        CustomViewDialog dialog = new CustomViewDialog();
        dialog.Builder(this, true)
                .dialogWithTwoButtons()
                .setTitle(getString(R.string.edit_item))
                .addCustomView(view)
                .onButtonClick(() -> {

                    if (item.getName() != null){
                        String name = nameTxt.getText().toString();
                        String oldName = item.getName();

                        for(ListItem i : item.getParentList()){
                            if (i.getName().equals(name) && i != item){
                                dialog.dismiss();
                                new MessageDialog().Builder(this,true)
                                        .setTitle(getString(R.string.item_with_name_already_exists))
                                        .onDismissListener(d-> dialog.show())
                                        .show();
                                return;
                            }
                        }
                        if (!oldName.equals(name)){
                            isEdited = true;
                            if (adapter.itemCountInJSONList > 1) editAllItemsWithSameKey(oldName,name,item);
                        }

                        item.setName(name);
                    }

                    if (!item.isArray() && !item.isObject()){
                        String value = valueTxt.getText().toString();
                        if (!item.getValue().equals(value))
                            isEdited = true;
                        item.setValue(value);
                    }

                    dialog.dismiss();
                    adapter.notifyItemChanged(pos);
                    updateFilterList(data.getCurrentList());
                })
                .show();
        Objects.requireNonNull(dialog.dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void editAllItemsWithSameKey(String oldName, String name, ListItem item){
        BasicDialog renameAllDialog = new BasicDialog();
        renameAllDialog.Builder(this,true)
                .setTitle(getString(R.string.rename_all))
                .setMessage(String.format(getString(R.string.rename_all_s_key_with_s),oldName,name))
                .onButtonClick(() -> {
                    for (ListItem listItem : adapter.getList()){
                        if (listItem.getName() != null && listItem.getName().equals(oldName) && listItem != item){
                            listItem.setName(name);
                            adapter.notifyItemRangeChanged(0,adapter.getItemCount());
                        }
                    }
                    renameAllDialog.dismiss();
                })
                .show();
    }

    public void DoneEdit(View view) {
        toggleEdit();
    }
}