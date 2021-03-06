package com.sjapps.jsonlist;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sjapps.about.AboutActivity;
import com.sjapps.adapters.ListAdapter;
import com.sjapps.library.customdialog.SetupDialog;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    ImageButton backBtn, menuBtn;
    Button openFileBtn;
    TextView titleTxt, emptyListTxt;
    ListView list;
    String path = "";
    ProgressBar progressBar;
    TextView loadingFileTxt;
    boolean isMenuOpen;
    ListAdapter adapter;
    ArrayList<ListItem> rootList = new ArrayList<>();
    View menu, dim_bg;
    ViewGroup viewGroup;
    AutoTransition autoTransition = new AutoTransition();
    Handler handler = new Handler();

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
        autoTransition.setDuration(150);
        menuBtn.setOnClickListener(view -> open_closeMenu());

        backBtn.setOnClickListener(view -> onBackPressed());
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


        Intent intent  = getIntent();
        Log.d(TAG, "onCreate: " + intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction())){

            Log.d(TAG, "onCreate: " + intent.getData());

            String FileData = FileSystem.LoadDataFromFile(this,intent.getData());
//            Log.d(TAG, "onCreate: " + FileData);
            if (FileData != null)
                LoadData(FileData);
            else
                Log.d(TAG, "onCreate: null data");
        }
        if (intent.getAction().equals("android.intent.action.OPEN_FILE")){
            ImportFromFile();
        }
    }

    private void OpenAbout() {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    @Override
    public void onBackPressed() {

        if (isMenuOpen) {
            open_closeMenu();
            return;
        }

        if (adapter.selectedItem != -1){
            adapter.selectedItem = -1;
            adapter.notifyDataSetChanged();
            return;
        }

        if(path.equals("")){
            SetupDialog dialog = new SetupDialog();
            String Title = "Exit?";
            String btn2Txt = "Yes";
            dialog.Short(this,Title,btn2Txt)
                    .onButtonClick(this::finish).show();
            return;
        }


        TransitionManager.beginDelayedTransition(viewGroup,autoTransition);



        String[] pathStrings = path.split("///");
        path = "";

        String Title = "";
        for(int i = 0; i < pathStrings.length-1; i++) {
            path = path.concat((path.equals("")?"":"///") + pathStrings[i]);

        }
        if (pathStrings.length > 1) {
            Title = pathStrings[pathStrings.length-2];
        }

        open(Title, path);
        if(path.equals("")) {
            backBtn.setVisibility(View.GONE);
        }
    }

    private void initialize(){
        backBtn = findViewById(R.id.backBtn);
        menuBtn = findViewById(R.id.menuBtn);
        titleTxt = findViewById(R.id.titleTxt);
        emptyListTxt = findViewById(R.id.emptyListTxt);
        list = findViewById(R.id.list);
        openFileBtn = findViewById(R.id.openFileBtn);
        viewGroup = findViewById(R.id.content);
        menu = findViewById(R.id.menu);
        dim_bg = findViewById(R.id.dim_layout);
        progressBar = findViewById(R.id.progressBar);
        loadingFileTxt = findViewById(R.id.LoadFileTxt);
        dim_bg.bringToFront();
        menu.bringToFront();
        menuBtn.bringToFront();
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


    private void LoadData(String Data){

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            ArrayList<ListItem> temp = rootList;
            JsonElement element;
            try {
                element = JsonParser.parseString(Data);

            }catch (OutOfMemoryError | Exception e){
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "File is too large", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
                return;
            }
            if (element == null){
                handler.post(() -> {
                    Toast.makeText(MainActivity.this, "Filed to load file!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
                return;
            }

            if (element instanceof JsonObject){
                Log.d(TAG, "run: Json object");
                JsonObject object = FileSystem.loadDataToJsonObj(element);
                Log.d(TAG, "LoadData: " + object);
                rootList = getJsonObject(object);
            }
            if (element instanceof JsonArray){
                Log.d(TAG, "run: Json array");
                JsonArray array = FileSystem.loadDataToJsonArray(element);
                Log.d(TAG, "LoadData: " + array);
                rootList = getJsonArrayRoot(array);
            }

            if (rootList != null) {
                handler.post(() -> {
                    TransitionManager.beginDelayedTransition(viewGroup,autoTransition);
                    adapter = new ListAdapter(rootList, MainActivity.this, "");
                    list.setAdapter(adapter);
                    openFileBtn.setVisibility(View.GONE);
                    backBtn.setVisibility(View.GONE);
                    titleTxt.setText("");
                    path = "";
                });

            }else rootList = temp;

            handler.post(() -> progressBar.setVisibility(View.GONE));

        }).start();


    }
    ArrayList<ListItem> getJsonArrayRoot(JsonArray array) {
        ArrayList<ListItem> Mainlist = new ArrayList<>();
        ListItem item = new ListItem();
        item.setName("Json Array");
        item.setIsArrayOfObjects(true);
        item.setListObjects(getJsonArray(array));
        Mainlist.add(item);

        return Mainlist;
    }

    ArrayList<ArrayList<ListItem>> getJsonArray(JsonArray array) {
        ArrayList<ArrayList<ListItem>> ArrList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) instanceof JsonObject) {
                ArrayList<ListItem> ListOfItems = getJsonObject((JsonObject) array.get(i));
                ArrList.add(ListOfItems);
            }
        }

        return ArrList;

    }

    boolean isArrayHasObjects(JsonArray array){
        for (int i = 0; i < array.size(); i++) {
            if (!(array.get(i) instanceof JsonObject)) {
                return false;
            }
        }

        return true;
    }

    ArrayList<ListItem> getJsonObject(JsonObject obj) {

        ArrayList<ListItem> Mainlist = new ArrayList<>();
        Set<String> keys = obj.keySet();

        Object[] keysArray = keys.toArray();

        try {


            for (Object o : keysArray) {
                ListItem item = new ListItem();
                item.setName(o.toString());

                if (obj.get(o.toString()) instanceof JsonObject) {
                    item.setIsObject(true);
                    ArrayList<ListItem> objList = getJsonObject((JsonObject) obj.get(o.toString()));
                    item.setObjects(objList);

                } else if (obj.get(o.toString()) instanceof JsonArray) {
                    JsonArray array = (JsonArray) obj.get(o.toString());
                    Log.d(TAG, "isArrayHasObjects: " + isArrayHasObjects(array));
                    if (isArrayHasObjects(array)){
                        item.setIsArrayOfObjects(true);
                        ArrayList<ArrayList<ListItem>> ArrList = getJsonArray(array);
                        item.setListObjects(ArrList);
                    }else{
                        item.setIsArray(true);
                        item.setValue(array.toString());
                    }

                } else {

                    item.setValue(obj.get(o.toString()).toString());

                }
                Mainlist.add(item);
            }
        }catch (Exception e){
            Log.e(TAG, "getJsonObject: Failed to load data");
            handler.post(() -> Toast.makeText(MainActivity.this, "Failed to load data!", Toast.LENGTH_SHORT).show());
            return null;
        }
        return Mainlist;

    }

    ArrayList<ListItem> getArrayList(ArrayList<ArrayList<ListItem>> list){
        ArrayList<ListItem> newList = new ArrayList<>();
        for(ArrayList<ListItem> lists : list) {
            newList.addAll(lists);
            newList.add(new ListItem().Space());
        }
        return newList;
    }
    ArrayList<ListItem> getListFromPath(){


        String[] pathStrings = path.split("///");

        ArrayList<ListItem> list = rootList;

        for (String pathString : pathStrings) {

            for (ListItem item : list) {

                if (item.getName() == null || !item.getName().equals(pathString)) {
                    continue;
                }

                if (item.isArrayOfObjects()) {
                    list = getArrayList(item.getListObjects());
                    break;
                }
                list = list.get(list.indexOf(item)).getObjects();
                if (list == null)
                    list = new ArrayList<>();
                break;
            }
        }
        return list;

    }

    public void open(String Title,String path) {
        TransitionManager.beginDelayedTransition(viewGroup,autoTransition);

        if (isMenuOpen)
            open_closeMenu();

        if (emptyListTxt.getVisibility() == View.VISIBLE)
            emptyListTxt.setVisibility(View.GONE);

        this.path = path;
        titleTxt.setText(Title);
        ArrayList<ListItem> arrayList = getListFromPath();
        adapter = new ListAdapter(arrayList, this, path);
        list.setAdapter(adapter);
        if (arrayList.size() == 0){
            emptyListTxt.setVisibility(View.VISIBLE);
        }
        System.out.println("path = " + path);
        if(!path.equals("")) {
            backBtn.setVisibility(View.VISIBLE);
        }

    }

    private void ImportFromFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        ActivityResultData.launch(intent);
    }


    ActivityResultLauncher<Intent> ActivityResultData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
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
                    Uri uri = result.getData().getData();
                    progressBar.setVisibility(View.VISIBLE);
                    loadingFileTxt.setVisibility(View.VISIBLE);
                    new Thread(() -> {
                        String Data = FileSystem.LoadDataFromFile(MainActivity.this, uri);

                        if (Data == null) {
                            Log.d(TAG, "onActivityResult: null data");
                            return;
                        }
                        handler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            loadingFileTxt.setVisibility(View.GONE);
                            LoadData(Data);
                        });

                    }).start();

                }
            });
}