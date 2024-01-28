package com.sjapps.jsonlist;

import static com.sjapps.jsonlist.java.JsonFunctions.*;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sjapps.about.AboutActivity;
import com.sjapps.adapters.ListAdapter;
import com.sjapps.jsonlist.java.JsonData;
import com.sjapps.jsonlist.java.ListItem;
import com.sjapps.library.customdialog.BasicDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    ImageButton backBtn, menuBtn;
    ImageView fileImg;
    Button openFileBtn;
    TextView titleTxt, emptyListTxt;
    RecyclerView list;
    JsonData data = new JsonData();
    LinearLayout progressView;
    ProgressBar progressBar;
    boolean isMenuOpen;
    ListAdapter adapter;
    View menu, dim_bg;
    ViewGroup viewGroup;
    AutoTransition autoTransition = new AutoTransition();
    Handler handler = new Handler();
    Thread readFileThread;
    RelativeLayout dropTarget;

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();

        setAnimation(this,fileImg,R.anim.scale_in_file_img, new DecelerateInterpolator());
        setAnimation(this,openFileBtn,R.anim.button_pop, new OvershootInterpolator());

        autoTransition.setDuration(150);
        menuBtn.setOnClickListener(view -> open_closeMenu());

        backBtn.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        openFileBtn.setOnClickListener(view -> ImportFromFile());

        menu.findViewById(R.id.openFileBtn2).setOnClickListener(view -> {
            ImportFromFile();
            open_closeMenu();
        });
        menu.findViewById(R.id.aboutBtn).setOnClickListener(view -> {
            OpenAbout();
            open_closeMenu();
        });
        dim_bg.setOnClickListener(view -> open_closeMenu());


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

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    dropTarget.setAlpha(1);
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    if (event.getClipDescription().hasMimeType("application/json")) {
                        dropTargetBackground.setBackgroundColor(setColor(R.attr.colorPrimary));
                        dropTargetBackground.setAlpha(.8f);
                    }else {
                        dropTargetTxt.setText(R.string.this_is_not_json_file);
                        dropTargetBackground.setBackgroundColor(setColor(R.attr.colorError));
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    dropTargetTxt.setText(R.string.drop_json_file_here);
                    dropTargetBackground.setBackgroundColor(setColor(R.attr.colorOnBackground));
                    dropTargetBackground.setAlpha(.5f);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    dropTargetTxt.setText(R.string.drop_json_file_here);
                    dropTargetBackground.setBackgroundColor(setColor(R.attr.colorOnBackground));
                    dropTarget.setAlpha(0);
                    return true;

                case DragEvent.ACTION_DROP:
                    if (!event.getClipDescription().hasMimeType("application/json"))
                        return false;
                    if (readFileThread != null && readFileThread.isAlive()) {
                        Snackbar.make(getWindow().getDecorView(),"Loading file in progress, try again later", BaseTransientBottomBar.LENGTH_SHORT).show();
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

    private void OpenAbout() {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (isMenuOpen) {
                open_closeMenu();
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
                        .setTitle("Exit?")
                        .setRightButtonText("Yes")
                        .onButtonClick(() ->{
                                dialog.dismiss();
                                MainActivity.this.finish();
                        })
                        .show();
                return;
            }

            TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
            data.goBack();
            open(JsonData.getPathFormat(data.getPath()), data.getPath(),-1);
            if (data.isEmptyPath()) {
                backBtn.setVisibility(View.GONE);
            }
        }
    };

    private void initialize() {
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
        backBtn = findViewById(R.id.backBtn);
        menuBtn = findViewById(R.id.menuBtn);
        titleTxt = findViewById(R.id.titleTxt);
        emptyListTxt = findViewById(R.id.emptyListTxt);
        list = findViewById(R.id.list);
        openFileBtn = findViewById(R.id.openFileBtn);
        viewGroup = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        dim_bg = findViewById(R.id.dim_layout);
        progressView = findViewById(R.id.loadingView);
        progressBar = findViewById(R.id.progressBar);
        fileImg = findViewById(R.id.fileImg);
        dim_bg.bringToFront();
        menu.bringToFront();
        menuBtn.bringToFront();
        dropTarget = findViewById(R.id.dropTarget);
        list.setLayoutManager(new LinearLayoutManager(this));
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


    private void LoadData(String Data) {

        loadingStarted("loading json");
        emptyListTxt.setVisibility(View.GONE);

        readFileThread = new Thread(() -> {
            ArrayList<ListItem> temp = data.getRootList();
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

            loadingStarted("creating list");
            try {
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
            } catch (Exception e){
                e.printStackTrace();
                creatingListException();
                data.setRootList(null);
            }

            if (!data.isRootListNull()) {
                handler.post(() -> {
                    TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
                    adapter = new ListAdapter(data.getRootList(), MainActivity.this, "");
                    list.setAdapter(adapter);
                    fileImg.clearAnimation();
                    openFileBtn.clearAnimation();
                    fileImg.setVisibility(View.GONE);
                    openFileBtn.setVisibility(View.GONE);
                    setAnimation(MainActivity.this,list,R.anim.scale_in2,new DecelerateInterpolator());
                    list.setVisibility(View.VISIBLE);
                    backBtn.setVisibility(View.GONE);
                    titleTxt.setText("");
                    data.clearPath();
                });

            } else data.setRootList(temp);

            handler.post(() -> loadingFinished(true));

        });
        readFileThread.start();
    }

    public void open(String Title, String path, int previousPosition) {
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);

        if (isMenuOpen)
            open_closeMenu();

        if (emptyListTxt.getVisibility() == View.VISIBLE)
            emptyListTxt.setVisibility(View.GONE);

        data.setPath(path);
        titleTxt.setText(Title);
        ArrayList<ListItem> arrayList = getListFromPath(path,data.getRootList());
        adapter = new ListAdapter(arrayList, this, path);
        list.setAdapter(adapter);

        if (previousPosition == -1) {
            handler.postDelayed(() -> {
                list.smoothScrollToPosition(data.getPreviousPos()+4);
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
        if (!path.equals("")) {
            backBtn.setVisibility(View.VISIBLE);
        }

    }

    private void ImportFromFile() {
        if (readFileThread != null && readFileThread.isAlive()) {
            Snackbar.make(getWindow().getDecorView(),"Loading file in progress, try again later", BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        ActivityResultData.launch(intent);
    }


    ActivityResultLauncher<Intent> ActivityResultData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    if(result.getResultCode() == Activity.RESULT_CANCELED){
                        Toast.makeText(MainActivity.this,"Import data canceled",Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                if (result.getData() == null || result.getData().getData() == null){
                    Toast.makeText(MainActivity.this, "Fail to load data", Toast.LENGTH_SHORT).show();
                    return;
                }
                //File
                ReadFile(result.getData().getData());
            });

    void ReadFile(Uri uri){
        if (readFileThread != null && readFileThread.isAlive()){
            return;
        }
        loadingStarted("Reading file");

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
            readFileThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void loadingStarted(){
        loadingStarted("loading...");

    }

    void loadingStarted(String txt){
        TextView text =  progressView.findViewById(R.id.loadingTxt);
        progressBar.setIndeterminate(true);
        text.setText(txt);
        handler.postDelayed(() -> {
            if (progressView.getVisibility() != View.VISIBLE) {
                setAnimation(this, progressView, R.anim.scale_in);
                text.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.VISIBLE);
            }
        },300);

    }
    public void updateProgress(int val){
        progressBar.setIndeterminate(false);
        progressBar.setProgress(val);
    }

    void loadingFinished(boolean isFinished){

        if (!isFinished){
            handler.postDelayed(()-> {
                setAnimation(this, progressView,R.anim.scale_out);
                progressView.setVisibility(View.INVISIBLE);
            },300);
            return;
        }

        progressBar.setIndeterminate(false);
        progressBar.setProgress(100);

        TextView text =  progressView.findViewById(R.id.loadingTxt);
        handler.postDelayed(() -> text.setText( "finished"),500);
        handler.postDelayed(() -> {
        },700);
        handler.postDelayed(() -> text.setVisibility(View.INVISIBLE),900);
        handler.postDelayed(() -> {
            setAnimation(this, progressView,R.anim.scale_out);
            progressView.setVisibility(View.INVISIBLE);
        },1000);
    }


    int setColor(int resid){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(resid, typedValue, true);
        return typedValue.data;
    }

    public static void setAnimation(Context context, @NonNull View view, @AnimRes int animationRes) {
        setAnimation(context,view,animationRes,null);
    }

    public static void setAnimation(Context context, @NonNull View view, @AnimRes int animationRes, Interpolator interpolator) {
        Animation animation = AnimationUtils.loadAnimation(context, animationRes);
        if (interpolator != null)
            animation.setInterpolator(interpolator);
        view.startAnimation(animation);
    }

    void fileTooLargeException(){
        postMessageException("File is too large");
    }
    void fileNotLoadedException(){
        postMessageException("Fail to load file!");
    }
    void creatingListException(){
        postMessageException("Fail to create list!");
    }
    void postMessageException(String msg){
        handler.post(() -> {
            Toast.makeText(MainActivity.this,msg, Toast.LENGTH_SHORT).show();
            loadingFinished(false);
        });
    }
}