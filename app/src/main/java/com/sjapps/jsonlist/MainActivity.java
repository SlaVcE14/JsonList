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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragAndDropPermissions;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import com.sj14apps.jsonlist.core.JsonFunctions;
import com.sj14apps.jsonlist.core.JsonNode;
import com.sj14apps.jsonlist.core.Path;
import com.sj14apps.jsonlist.core.controllers.SearchController;
import com.sjapps.about.AboutActivity;
import com.sjapps.adapters.ListAdapter;
import com.sjapps.adapters.PathListAdapter;
import com.sjapps.jsonlist.controllers.AndroidDragAndDrop;
import com.sjapps.jsonlist.controllers.AndroidEditController;
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
import com.sjapps.jsonlist.controllers.AndroidSearchController;
import com.sjapps.jsonlist.databinding.ActivityMainBinding;
import com.sjapps.library.customdialog.BasicDialog;
import com.sjapps.library.customdialog.DialogButtonEvents;
import com.sjapps.library.customdialog.ListDialog;
import com.sjapps.logs.CustomExceptionHandler;
import com.sjapps.logs.LogActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    public ActivityMainBinding binding;

    public JsonData data = new JsonData();
    public ListAdapter adapter;
    PathListAdapter pathAdapter;
    public AutoTransition autoTransition = new AutoTransition();
    public Handler handler = new Handler();
    public Thread readFileThread;
    public AppState state;
    int listPrevDx = 0;
    public RawJsonView rawJsonView;
    FileManager fileManager;
    WebManager webManager;
    JsonLoader jsonLoader;
    public SearchController searchController;
    public AndroidEditController editController;

    public boolean isVertical = true;
    public boolean isUrlSearching;
    boolean isMenuOpen;
    boolean isTopMenuVisible;
    public boolean isLoading;
    public boolean unsavedChanges;

    public Guideline guideline;

    ArrayList<String> filterList = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
        checkCrashLogs();
        LoadStateData();
        checkHasNewVersion();
        Log.d(TAG, "onResume: resume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler))
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
            ReadFile(intent.getData(),null);
        }
        if (intent.getAction().equals("android.intent.action.OPEN_FILE")){
            fileManager.importFromFile();
        }
        if (intent.getAction().equals("android.intent.action.OPEN_URL")){
            showUrlSearchView();
        }

        functions.setAnimation(this,binding.fileImg,R.anim.scale_in_file_img, new DecelerateInterpolator());
        functions.setAnimation(this,binding.openFileBtn,R.anim.button_pop, new OvershootInterpolator());

        autoTransition.setDuration(150);
    }

    private void initialize() {
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        guideline = binding.guidelineHorizontal;

        binding.dimLayout.bringToFront();
        binding.menu.getRoot().bringToFront();
        binding.menuBtn.bringToFront();

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

        binding.list.setLayoutManager(layoutManager);
        binding.pathList.setLayoutManager(pathLM);
        binding.searchResultList.setLayoutManager(new LinearLayoutManager(this));

        fileManager = new AndroidFileManager(this,handler);
        jsonLoader = new AndroidJsonLoader(this);
        searchController = new AndroidSearchController(this);
        editController = new AndroidEditController(this);

        new AndroidDragAndDrop(this, dragAndDropCallback);

    }

    private void setLayoutBounds() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.content, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets insetsN = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout());

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            layoutParams.leftMargin = insets.left + insetsN.left;
            layoutParams.topMargin = insets.top;
            layoutParams.rightMargin = insets.right + insetsN.right;
            layoutParams.bottomMargin = insets.bottom;
            v.setLayoutParams(layoutParams);

            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            binding.searchLL.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    imeInsets.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setEvents(){
        binding.menuBtn.setOnClickListener(view -> open_closeMenu());

        binding.backBtn.setOnClickListener(view -> {
            if(canCallBackDispatcher()) getOnBackPressedDispatcher().onBackPressed();
        });
        binding.openFileBtn.setOnClickListener(view -> fileManager.importFromFile());
        binding.openUrlBtn.setOnClickListener(view -> showUrlSearchView());

        binding.titleTxt.setOnClickListener(v -> {
            if (!data.isEmptyPath())
                showHidePathList();
        });

        binding.pathListBG.setOnClickListener(v -> showHidePathList());

        //TODO Web
        binding.urlSearch.setOnEditorActionListener((v, actionId, event) -> {
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

        searchController.setEvents(data);

        binding.menu.openFileBtn2.setOnClickListener(view -> {
            fileManager.importFromFile();
            open_closeMenu();
        });
        binding.menu.searchUrlBtn.setOnClickListener(view -> {
            open_closeMenu();
            showUrlSearchView();
        });
        binding.menu.settingsBtn.setOnClickListener(view -> {
            OpenSettings();
            open_closeMenu();
        });
        binding.menu.aboutBtn.setOnClickListener(view -> {
            OpenAbout();
            open_closeMenu();
        });
        binding.menu.logBtn.setOnClickListener(view -> {
            OpenLogPage();
            open_closeMenu();
        });
        binding.dimLayout.setOnClickListener(view -> open_closeMenu());
        binding.splitViewBtn.setOnClickListener(view -> rawJsonView.toggleSplitView());
        binding.splitViewBtn.setOnLongClickListener(view -> {
            ((AndroidRawJsonView) rawJsonView).ShowDebugJson();
            return true;
        });
        binding.filterBtn.setOnClickListener(view -> filter());
        binding.searchBtn.setOnClickListener(view -> searchController.showSearchView());
        binding.editBtn.setOnClickListener(view -> editController.toggleEdit());
        binding.saveBtn.setOnClickListener(view -> saveChanges());

        FrameLayout resizeSplitViewBtn = binding.resizeSplitViewBtn;

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
                        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
                        if (isVertical){
                            params.guidePercent = (event.getRawY() - binding.mainLL.getY()) / binding.content.getHeight();
                        }else {
                            params.guidePercent = (event.getRawX() - binding.mainLL.getX()) / binding.content.getWidth();
                        }
                        if (params.guidePercent < 0.2f){
                            params.guidePercent = 0.03f;
                            if (binding.listRL.getVisibility() == VISIBLE)
                                resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                            binding.listRL.setVisibility(GONE);
                        }else {
                            if (binding.listRL.getVisibility() == GONE)
                                resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                            binding.listRL.setVisibility(VISIBLE);
                        }
                        if (params.guidePercent > 0.8f)
                            params.guidePercent = 0.8f;
                        guideline.setLayoutParams(params);
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
            if (binding.pathListBG.getVisibility() == VISIBLE){
                showHidePathList();
                return;
            }

            if (isMenuOpen) {
                open_closeMenu();
                return;
            }

            if (editController.isEditMode){
                editController.toggleEdit();
                return;
            }

            if (binding.searchUrlView.getVisibility() == VISIBLE){
                hideUrlSearchView();
                return;
            }
            if (binding.searchLL.getVisibility() == VISIBLE){
                searchController.hideSearchView();
                return;
            }

            if (binding.listRL.getVisibility() == GONE){
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
                    showUnsavedChangesDialog(new DialogButtonEvents() {
                        @Override
                        public void onLeftButtonClick() {
                            MainActivity.this.finish();
                        }

                        @Override
                        public void onRightButtonClick() {
                            saveChanges();
                        }
                    });
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
            TransitionManager.endTransitions(binding.content);
            TransitionManager.beginDelayedTransition(binding.content, autoTransition);
            data.goBack();
            open(JsonData.getPathFormat(data.getPath().toString()), data.getCurrentNode().parent,data.getPath(),-1);
        }
    };

    public void showUnsavedChangesDialog(DialogButtonEvents buttonEvents){
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
                        buttonEvents.onLeftButtonClick();
                    }

                    @Override
                    public void onRightButtonClick() {
                        dialog.dismiss();
                        buttonEvents.onRightButtonClick();
                    }
                })
                .show();
    }

    public void saveChanges() {
        if ((readFileThread != null && readFileThread.isAlive())) {
            if (readFileThread.getName().equals("writeFileThread"))
                Snackbar.make(getWindow().getDecorView(), R.string.saving_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            else Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

        if (isUrlSearching) {
            Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

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
        TextView logBtn = binding.menu.logBtn;
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
            binding.menuBtn.setImageResource(R.drawable.menu_with_dot);
            return;
        }
        getTheme().resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);
        logBtn.setTextColor(typedValue.data);
        logBtn.setBackgroundResource(R.drawable.ripple_list2);
        binding.menuBtn.setImageResource(R.drawable.ic_menu);
    }

    void checkHasNewVersion() {
        if (!state.isAutoCheckForUpdate())
            return;

        long currentSeconds = System.currentTimeMillis()/1000;

        TypedValue typedValue = new TypedValue();
        TextView aboutBtn = binding.menu.aboutBtn;

        getTheme().resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);
        aboutBtn.setTextColor(typedValue.data);
        aboutBtn.setBackgroundResource(R.drawable.ripple_list2);

        if (currentSeconds - state.getLastCheckForUpdate() < 86400){
            return;
        }

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(MainActivity.this.getPackageName(), 0);
            int currentVersionCode = packageInfo.versionCode;
            WebManager webManager = new WebManager();
            webManager.checkHasNewVersion(state, AboutActivity.APP_INFO_URL, currentVersionCode, new WebManager.CheckNewVersionCallback() {
                @Override
                public void saveState() {
                    FileSystem.SaveState(MainActivity.this,state);
                }

                @Override
                public void updateUI() {
                    getTheme().resolveAttribute(R.attr.colorOnPrimary, typedValue, true);
                    aboutBtn.setTextColor(typedValue.data);
                    aboutBtn.setBackgroundResource(R.drawable.ripple_button);
                    binding.menuBtn.setImageResource(R.drawable.menu_with_dot);
                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


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

    public void showToolbar() {
        if (editController.isEditMode)
            return;
        if (editController.isApplyingChanges)
            return;
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching)
            return;


        binding.floatingToolbar.animate().cancel();

        isTopMenuVisible = true;
        binding.floatingToolbar.setVisibility(VISIBLE);
        binding.floatingToolbar.animate()
                .translationY(0)
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setInterpolator(new OvershootInterpolator(1.1f))
                .setDuration(500)
                .start();

    }

    public void hideToolbar() {
        binding.floatingToolbar.animate().cancel();

        isTopMenuVisible = false;
        binding.floatingToolbar.animate()
                .translationY(binding.floatingToolbar.getHeight()+50)
                .setDuration(300)
                .scaleX(.5f)
                .scaleY(.5f)
                .alpha(0)
                .withEndAction(()-> binding.floatingToolbar.setVisibility(GONE))
                .start();
    }

    private void open_closeMenu() {
        if (!isMenuOpen) {
            binding.dimLayout.setVisibility(VISIBLE);
            binding.menu.getRoot().setVisibility(VISIBLE);
            binding.menuBtn.setImageResource(R.drawable.ic_close);
            isMenuOpen = true;
        } else {
            binding.dimLayout.setVisibility(INVISIBLE);
            binding.menu.getRoot().setVisibility(GONE);
            binding.menuBtn.setImageResource(R.drawable.ic_menu);
            isMenuOpen = false;
        }
    }

    private void showUrlSearchView() {
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching) {
            Snackbar.make(getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

        if (unsavedChanges){
            showUnsavedChangesDialog(new DialogButtonEvents() {
                @Override
                public void onLeftButtonClick() {
                    unsavedChanges = false;
                    binding.saveBtn.setVisibility(GONE);
                    showUrlSearchView();
                }

                @Override
                public void onRightButtonClick() {
                    saveChanges();
                }
            });
            return;
        }

        if (binding.searchLL.getVisibility() == VISIBLE){
            searchController.hideSearchView();
        }


        TransitionManager.beginDelayedTransition(binding.content, autoTransition);
        binding.mainLL.setVisibility(GONE);
        binding.searchUrlView.setVisibility(VISIBLE);

        if (binding.backBtn.getVisibility() == GONE)
            binding.backBtn.setVisibility(VISIBLE);

        binding.urlSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.urlSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideUrlSearchView() {
        binding.searchUrlView.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(binding.content, autoTransition);
        binding.mainLL.setVisibility(VISIBLE);
        binding.urlSearch.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(binding.urlSearch.getWindowToken(), 0);

        if (data.isEmptyPath()) {
            binding.backBtn.setVisibility(GONE);
        }

    }

    public void open(String Title, JsonNode node, Path path, int previousPosition){
        TransitionManager.endTransitions(binding.content);
        TransitionManager.beginDelayedTransition(binding.content, autoTransition);

        if (isMenuOpen)
            open_closeMenu();

        if (binding.emptyListTxt.getVisibility() == VISIBLE)
            binding.emptyListTxt.setVisibility(GONE);

        if (node.isObject && node.parent != null && node.parent.isArray){
            open(Title,node.parent,path,previousPosition);
            return;
        }

        pathAdapter = new PathListAdapter(this,path);
        binding.pathList.setAdapter(pathAdapter);
        data.setPath(path);
        binding.titleTxt.setText(Title);

        ArrayList<ListItem> arrayList = getListFromNode(node);
        data.setCurrentNode(node);
        updateFilterList(arrayList);
        adapter = new ListAdapter(arrayList, this, path);
        binding.list.setAdapter(adapter);

        if (previousPosition == -1) {
            handler.postDelayed(() -> {
                if (state.isScrollAnimation()) binding.list.smoothScrollToPosition(data.getPreviousPos()+2);
                else binding.list.scrollToPosition(data.getPreviousPos()+2);
                adapter.setHighlightItem(data.getPreviousPos());
            }, 500);
            handler.postDelayed(() -> adapter.notifyItemChanged(data.getPreviousPos()), 600);
        }
        else data.addPreviousPos(previousPosition);

        if (arrayList.isEmpty()) {
            binding.emptyListTxt.setVisibility(VISIBLE);
        }
        System.out.println("path = " + path);
        if (!data.isEmptyPath()) {
            binding.backBtn.setVisibility(VISIBLE);
        } else binding.backBtn.setVisibility(GONE);
    }

    public void highlightItem(int id){
        handler.postDelayed(() -> {
            if (state.isScrollAnimation()) binding.list.smoothScrollToPosition(id+2);
            else binding.list.scrollToPosition(id+2);
            adapter.setHighlightItem(id);
        }, 500);
        handler.postDelayed(() -> {
            adapter.notifyItemChanged(id);
        }, 600);
    }

    public void goBack(int n){
        if (binding.pathListBG.getVisibility() == VISIBLE)
            showHidePathList();
        for (int i = 0; i<n; i++)
            data.goBack();
        data.setCurrentNode(JsonFunctions.getNodeFromPath(data.getRootNode(),data.getPath()));
        open(JsonData.getPathFormat(data.getPath().toString()),data.getCurrentNode(),data.getPath(),-1);

    }

    private boolean canCallBackDispatcher(){
        return !data.isEmptyPath() ||
                binding.searchUrlView.getVisibility() == VISIBLE ||
                binding.searchLL.getVisibility() == VISIBLE ||
                (adapter != null && adapter.isEditMode());
    }

    public void showBackBtn() {
        if (binding.backBtn.getVisibility() == VISIBLE)
            return;
        binding.backBtn.setVisibility(VISIBLE);
    }

    public void hideBackBtnIfNotNeeded() {
        if (data.isEmptyPath())
            binding.backBtn.setVisibility(GONE);
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
        binding.list.setAdapter(adapter);
    }

    public void updateFilterList(ArrayList<ListItem> items) {
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

        if (editController.isEditMode)
            return;

        if (binding.searchLL.getVisibility() == VISIBLE)
            return;

        if (binding.pathListBG.getVisibility() == VISIBLE) {
            binding.pathListBG.setVisibility(GONE);
            return;
        }

        binding.pathListBG.setVisibility(VISIBLE);
    }

    public void ShowList() {
        TransitionManager.endTransitions(binding.content);
        TransitionManager.beginDelayedTransition(binding.content, autoTransition);

        binding.resizeSplitViewBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        binding.listRL.setVisibility(VISIBLE);
        guideline.setGuidelinePercent(.5f);
    }

    private void updateOrientation(){

        int initWidth = functions.dpToPixels(this,100);
        int initHeight = functions.dpToPixels(this,7);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.resizeSplitViewBtn.getLayoutParams();
        FrameLayout.LayoutParams paramsLine = (FrameLayout.LayoutParams) binding.resizeSplitViewBtnLine.getLayoutParams();


        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.mainLL);

        float percent = rawJsonView.showJson ? 0.5f : 1f;

        constraintSet.clear(R.id.resizeSplitViewBtn);
        constraintSet.clear(R.id.listRL);
        constraintSet.clear(R.id.rawJsonRL);

        if (!isVertical){
            guideline = binding.guidelineVertical;

            constraintSet.setGuidelinePercent(binding.guidelineVertical.getId(),percent);

            binding.guidelineVertical.setVisibility(VISIBLE);
            binding.guidelineHorizontal.setVisibility(GONE);

            paramsLine.width = initHeight;
            paramsLine.height = initWidth;

            params.leftMargin = functions.dpToPixels(this,-15);
            params.topMargin = 0;
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = binding.rawJsonRL.getId();
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

            constraintSet.connect(R.id.listRL,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
            constraintSet.connect(R.id.listRL,ConstraintSet.END,guideline.getId(),ConstraintSet.START);
            constraintSet.connect(R.id.listRL,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
            constraintSet.connect(R.id.listRL,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);

            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.START,guideline.getId(),ConstraintSet.START);
            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
            constraintSet.connect(R.id.rawJsonRL,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);

            constraintSet.applyTo(binding.mainLL);
            return;
        }
        guideline = binding.guidelineHorizontal;

        constraintSet.setGuidelinePercent(binding.guidelineHorizontal.getId(),percent);
        binding.guidelineHorizontal.setVisibility(VISIBLE);
        binding.guidelineVertical.setVisibility(GONE);

        paramsLine.width = initWidth;
        paramsLine.height = initHeight;

        params.leftMargin = 0;
        params.topMargin = functions.dpToPixels(this,-15);
        params.topToTop = binding.rawJsonRL.getId();
        params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        constraintSet.connect(R.id.listRL,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
        constraintSet.connect(R.id.listRL,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
        constraintSet.connect(R.id.listRL,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
        constraintSet.connect(R.id.listRL,ConstraintSet.BOTTOM,guideline.getId(),ConstraintSet.BOTTOM);

        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START);
        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.TOP,guideline.getId(),ConstraintSet.TOP);
        constraintSet.connect(R.id.rawJsonRL,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);

        constraintSet.applyTo(binding.mainLL);

    }

    void ReadFile(Uri uri, FileManager.FileReadingCallback callback){
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching){
            return;
        }
        ((AndroidFileManager) fileManager).validatePath(uri);

        loadingStarted(getString(R.string.reading_file));

        readFileThread = new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                AssetFileDescriptor fileDescriptor = getContentResolver().openAssetFileDescriptor(uri, "r");
                if (fileDescriptor == null) {
                    handler.post(fileCallback::onFileLoadFailed);
                    if (callback != null) handler.post(callback::onFileReadingFinished);
                    return;
                }

                String fileName = AndroidFileManager.getFileName(this,uri);
                long fileSize = fileDescriptor.getLength();

                fileDescriptor.close();

                fileManager.readFile(inputStream, fileName , fileSize, fileCallback);
                if (callback != null) handler.post(callback::onFileReadingFinished);

            } catch (IOException e) {
                e.printStackTrace();
                handler.post(fileCallback::onFileLoadFailed);
                if (callback != null) handler.post(callback::onFileReadingFinished);
            }
        });
        readFileThread.setName("readFileThread");
        readFileThread.start();
        hideToolbar();
    }

    void WriteFile(Uri uri){
        if ((readFileThread != null && readFileThread.isAlive()) || isUrlSearching){
            return;
        }
        loadingStarted(getString(R.string.saving_file));
        hideToolbar();

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri,"wt");

            readFileThread = new Thread(() -> {
                String dataStr = data.getRawData();
                if (dataStr.equals("-1"))
                    dataStr = JsonFunctions.convertToRawString(data.getRootNode());

                fileManager.writeFile(outputStream, dataStr, fileWriteCallback);
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
        webManager.getFromUrl(binding.urlSearch.getText().toString(),webCallback);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.urlSearch.getWindowToken(), 0);
    }

    public void loadingStarted(){
        loadingStarted(getString(R.string.loading));

    }

    public void loadingStarted(String txt){
        isLoading = true;
        TextView text =  binding.progressTxt;
        binding.progressBar.setIndeterminate(true);
        text.setText(txt);
        handler.postDelayed(() -> {
            if (binding.progressView.getVisibility() != VISIBLE) {
                functions.setAnimation(this, binding.progressView, R.anim.scale_in);
                text.setVisibility(VISIBLE);
                binding.progressView.setVisibility(VISIBLE);
            }
        },300);

    }

    public void loadingFinished(boolean isFinished){

        if (!isFinished){
            isLoading = false;
            handler.postDelayed(()-> {
                functions.setAnimation(this, binding.progressView,R.anim.scale_out);
                binding.progressView.setVisibility(INVISIBLE);
            },300);
            return;
        }

        binding.progressBar.setIndeterminate(false);
        binding.progressBar.setProgressCompat(100,true);

        TextView text =  binding.progressTxt;
        handler.postDelayed(() -> text.setText( R.string.finished),500);
        handler.postDelayed(() -> {
        },700);
        handler.postDelayed(() -> text.setVisibility(INVISIBLE),900);
        handler.postDelayed(() -> {
            functions.setAnimation(this, binding.progressView,R.anim.scale_out);
            binding.progressView.setVisibility(INVISIBLE);
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
                binding.progressBar.setProgressCompat(progress,true);
            });
        }
    };

    private final FileManager.FileWriteCallback fileWriteCallback = new FileManager.FileWriteCallback() {
        @Override
        public void onFileWriteSuccess() {
            unsavedChanges = false;
            loadingFinished(true);
            binding.saveBtn.setVisibility(GONE);
        }

        @Override
        public void onFileWriteFail() {
            loadingFinished(false);
            Toast.makeText(MainActivity.this, getString(R.string.fail_to_save), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgressUpdate(int progress) {
            binding.progressBar.setProgressCompat(progress,true);
        }
    };

    JsonLoader.JsonLoaderCallback jsonLoaderCallback = new JsonLoader.JsonLoaderCallback() {
        @Override
        public void start() {
            loadingStarted(getString(R.string.loading_json));
            binding.emptyListTxt.setVisibility(GONE);
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
                TransitionManager.beginDelayedTransition(binding.content, autoTransition);

                if (binding.searchUrlView.getVisibility() == VISIBLE)
                    hideUrlSearchView();

                if (binding.searchLL.getVisibility() == VISIBLE)
                    searchController.hideSearchView();

                data.setCurrentNode(data.getRootNode());
                updateFilterList(data.getRootList());
                adapter = new ListAdapter(data.getCurrentList(), MainActivity.this, new Path());
                pathAdapter = new PathListAdapter(MainActivity.this,data.getPath());
                binding.list.setAdapter(adapter);
                binding.pathList.setAdapter(pathAdapter);
                binding.fileImg.clearAnimation();
                binding.openFileBtn.clearAnimation();
                binding.fileImg.setVisibility(GONE);
                binding.openFileBtn.setVisibility(GONE);
                binding.openUrlBtn.setVisibility(GONE);
                functions.setAnimation(MainActivity.this,binding.list,R.anim.scale_in2,new DecelerateInterpolator());
                binding.list.setVisibility(VISIBLE);
                binding.backBtn.setVisibility(GONE);
                binding.saveBtn.setVisibility(GONE);
                unsavedChanges = false;
                binding.titleTxt.setText("");
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
            hideToolbar();
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
            if (editController.isEditMode){
                Snackbar.make(getWindow().getDecorView(), R.string.editing_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
                return true;
            }

            return false;
        }

        @Override
        public void onDrop(Uri uri, DragAndDropPermissions permissions) {
            ReadFile(uri, () -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && permissions != null)
                    permissions.release();
            });
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
                ReadFile(result.getData().getData(),null);
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
        editController.editItem(pos, adapter.getList().get(pos));
    }

    public void DoneEdit(View view) {
        editController.toggleEdit();
    }
}