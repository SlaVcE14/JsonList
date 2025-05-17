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
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import com.sj14apps.jsonlist.core.JsonFunctions;
import com.sjapps.about.AboutActivity;
import com.sjapps.adapters.ListAdapter;
import com.sjapps.adapters.PathListAdapter;
import com.sjapps.jsonlist.controllers.AndroidDragAndDrop;
import com.sjapps.jsonlist.controllers.AndroidFileManager;
import com.sjapps.jsonlist.controllers.AndroidJsonLoader;
import com.sjapps.jsonlist.controllers.AndroidRawJsonView;
import com.sjapps.jsonlist.controllers.AndroidWebManager;
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
import com.sjapps.logs.CustomExceptionHandler;
import com.sjapps.logs.LogActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    ImageButton backBtn, menuBtn, splitViewBtn, filterBtn;
    View editBtn;
    ImageView fileImg;
    Button openFileBtn;
    Button openUrlBtn;
    EditText urlSearch;
    LinearLayout urlLL;
    LinearLayout messageLL;
    TextView titleTxt, emptyListTxt;
    RecyclerView list;
    RecyclerView pathList;
    public JsonData data = new JsonData();
    public LinearLayout progressView;
    LinearLayout mainLL;
    LinearProgressIndicator progressBar;
    ListAdapter adapter;
    PathListAdapter pathAdapter;
    View menu, dim_bg, pathListView;
    public WebView rawJsonWV;
    public ViewGroup viewGroup;
    public AutoTransition autoTransition = new AutoTransition();
    public Handler handler = new Handler();
    public Thread readFileThread;
    public RelativeLayout listRL;
    public RelativeLayout rawJsonRL;
    public AppState state;
    View fullRawBtn;
    LinearLayout topMenu;
    int listPrevDx = 0;
    RawJsonView rawJsonView;
    FileManager fileManager;
    WebManager webController;
    JsonLoader jsonLoader;

    public boolean isVertical = true;
    public boolean isUrlSearching;
    boolean isMenuOpen;
    boolean isTopMenuVisible;
    boolean isEdited;
    public boolean isEditMode;
    boolean unsavedChanges;
    FloatingActionButton saveFAB;

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
            mainLL.setOrientation(LinearLayout.HORIZONTAL);
            updateFullRawBtn();
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
        editBtn = findViewById(R.id.editBtn);
        titleTxt = findViewById(R.id.titleTxt);
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
        rawJsonWV = findViewById(R.id.rawJsonWV);
        menuBtn.bringToFront();
        fullRawBtn = findViewById(R.id.fullRawBtn);
        topMenu = findViewById(R.id.topMenu);
        saveFAB = findViewById(R.id.saveFAB);

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

        int textColor = functions.setColor(this,R.attr.colorOnSecondaryContainer);
        int keyColor = functions.setColor(this,R.attr.colorPrimary);
        int numberColor = functions.setColor(this,R.attr.colorTertiary);
        int booleanAndNullColor = functions.setColor(this,R.attr.colorError);
        int bgColor = functions.setColor(this,R.attr.colorSecondaryContainer);

        rawJsonView = new AndroidRawJsonView(this,textColor,keyColor,numberColor,booleanAndNullColor,bgColor);

        webController =  new AndroidWebManager(this);

        rawJsonView.updateRawJson("");

        list.setLayoutManager(layoutManager);
        pathList.setLayoutManager(pathLM);

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
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setEvents(){
        menuBtn.setOnClickListener(view -> open_closeMenu());

        backBtn.setOnClickListener(view -> {
            if(!data.isEmptyPath() || urlLL.getVisibility() == VISIBLE || (adapter != null && adapter.isEditMode())) getOnBackPressedDispatcher().onBackPressed();
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
        editBtn.setOnClickListener(view -> toggleEdit());
        saveFAB.setOnClickListener(view -> saveChanges());
    }

    private void toggleEdit() {
        if (adapter == null)
            return;

        isEditMode = !adapter.isEditMode();

        if (isEditMode){
            showBackBtn();
            hideTopMenu();
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
                saveFAB.setVisibility(VISIBLE);
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

        if (!isVertical){
            mainLL.setOrientation(LinearLayout.HORIZONTAL);
            updateFullRawBtn();
        }else {
            mainLL.setOrientation(LinearLayout.VERTICAL);
            updateFullRawBtn();
        }
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

            if (messageLL.getVisibility() == VISIBLE){
                toggleEdit();
                return;
            }

            if (urlLL.getVisibility() == VISIBLE){
                hideUrlSearchView();
                return;
            }

            if (listRL.getVisibility() == GONE){
                FullRaw(null);
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

    private void showTopMenu() {
        if (isEditMode)
            return;

        topMenu.animate().cancel();

        isTopMenuVisible = true;
        topMenu.setVisibility(VISIBLE);
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
                .withEndAction(()-> topMenu.setVisibility(GONE))
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


        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(GONE);
        urlLL.setVisibility(VISIBLE);

        if (backBtn.getVisibility() == GONE)
            backBtn.setVisibility(VISIBLE);

        urlSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(urlSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideUrlSearchView() {
        urlLL.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
        mainLL.setVisibility(VISIBLE);
        urlSearch.setText("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(urlSearch.getWindowToken(), 0);

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

/*    private void open_closeSplitView(){
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        if (rawJsonView.showJson){
            functions.setAnimation(this,rawJsonRL,isVertical?R.anim.slide_bottom_out:R.anim.slide_right_out,new AccelerateDecelerateInterpolator());
            handler.postDelayed(()-> rawJsonRL.setVisibility(View.GONE),400);
            rawJsonView.showJson = false;
            if (listRL.getVisibility() == View.GONE)
                listRL.setVisibility(View.VISIBLE);
            return;
        }
        rawJsonView.showJson = true;
        rawJsonRL.setVisibility(View.VISIBLE);
        functions.setAnimation(this,rawJsonRL,isVertical?R.anim.slide_bottom_in:R.anim.slide_right_in,new DecelerateInterpolator());
        if (!isRawJsonLoaded)
            ShowJSON();

    }*/

    public void showHidePathList() {

        if (pathListView.getVisibility() == VISIBLE) {
            pathListView.setVisibility(GONE);
            return;
        }

        pathListView.setVisibility(VISIBLE);
    }

    public void FullRaw(View view) {
        TransitionManager.endTransitions(viewGroup);
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        fullRawBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        if (listRL.getVisibility() == VISIBLE){
            listRL.setVisibility(GONE);
        }else
            listRL.setVisibility(VISIBLE);
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
/*

    private void ShowJSON(){

        if (data.getRawData().equals("-1")) {
            Snackbar.make(getWindow().getDecorView(), R.string.file_is_to_large_to_be_shown_in_a_split_screen, BaseTransientBottomBar.LENGTH_SHORT).show();
            if (progressView.getVisibility() == View.VISIBLE)
                loadingFinished(true);
            if (rawJsonView.showJson)
                open_closeSplitView();
            return;
        }
        if (data.getRawData().isEmpty())
            return;

        loadingStarted(getString(R.string.displaying_json));

        Thread thread = new Thread(() -> {
            String dataStr = JsonFunctions.getAsPrettyPrint(data.getRawData());
            handler.post(()-> {
                rawJsonView.updateRawJson(dataStr);
                loadingFinished(true);
                isRawJsonLoaded = true;
            });
        });
        thread.setName("loadingJson");
        thread.start();

    }
*/

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

    public void SearchUrl(View view) {
        SearchUrl();
    }

    private void SearchUrl() {
        webController.getFromUrl(urlSearch.getText().toString(),webCallback);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlSearch.getWindowToken(), 0);
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
            saveFAB.setVisibility(GONE);
        }

        @Override
        public void onFileWriteFail() {
            loadingFinished(false);
            Toast.makeText(MainActivity.this, getString(R.string.fail_to_save), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgressUpdate(int progress) {
            handler.post(()->{
                progressBar.setProgressCompat(progress,true);
            });
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
                saveFAB.setVisibility(GONE);
                unsavedChanges = false;
                titleTxt.setText("");
                data.clearPath();
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
                try {
                    loadingStarted(getString(R.string.saving_file));
                    OutputStream outputStream = getContentResolver().openOutputStream(result.getData().getData());
                    fileManager.writeFile(outputStream, data.getRawData(), fileWriteCallback); //TODO thread???
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });

    public void editItem(int pos) {
        View view = LayoutInflater.from(this).inflate(R.layout.edit_item,null);

        ListItem item = adapter.getList().get(pos);

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
                .setTitle("Edit Item")
                .addCustomView(view)
                .onButtonClick(() -> {
                    if (item.getName() != null){
                        item.setName(nameTxt.getText().toString());
                    }

                    if (!item.isArray() && !item.isObject())
                        item.setValue(valueTxt.getText().toString());

                    dialog.dismiss();
                    adapter.notifyItemChanged(pos);
                    isEdited = true;
                })
                .show();
    }

    public void DoneEdit(View view) {
        toggleEdit();
    }
}