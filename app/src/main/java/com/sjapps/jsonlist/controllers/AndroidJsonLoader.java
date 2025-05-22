package com.sjapps.jsonlist.controllers;

import static com.sj14apps.jsonlist.core.JsonFunctions.getJsonArrayRoot;
import static com.sj14apps.jsonlist.core.JsonFunctions.getJsonObject;

import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sj14apps.jsonlist.core.JsonData;
import com.sj14apps.jsonlist.core.controllers.JsonLoader;
import com.sj14apps.jsonlist.core.ListItem;

import java.util.ArrayList;

public class AndroidJsonLoader implements JsonLoader {

    MainActivity activity;

    public AndroidJsonLoader(MainActivity activity){
        this.activity = activity;
    }

    @Override
    public void LoadData(String Data, String fileName, JsonLoaderCallback callBack) {
        callBack.start();

        JsonData data = activity.data;

        activity.readFileThread = new Thread(() -> {
            ArrayList<ListItem> temp = data.getRootList();
            String tempRaw = data.getRawData();
            String tmpFileName = data.getFileName();
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
            activity.readFileThread.setName("readFileThread");
            callBack.started();
            try {
                data.setRootList(null);
                if (element instanceof JsonObject) {
                    JsonObject object = element.getAsJsonObject();
                    data.setRootList(getJsonObject(object));
                }
                if (element instanceof JsonArray) {
                    JsonArray array = element.getAsJsonArray();
                    data.setRootList(getJsonArrayRoot(array));
                }
                if (Data.length()<10000000)
                    data.setRawData(Data);
                else data.setRawData("-1");
                data.clearPreviousPos();
                data.setFileName(fileName);
            } catch (Exception e){
                e.printStackTrace();
                creatingListException();
                data.setRootList(null);
            }

            if (!data.isRootListNull()) {
                callBack.success();

            } else {
                data.setRootList(temp);
                data.setRawData(tempRaw);
                data.setFileName(tmpFileName);
                callBack.failed();
                fileNotLoadedException();
                return;
            }

            callBack.after();

        });
        activity.readFileThread.start();
    }

    void fileTooLargeException(){
        postMessageException(activity.getString(R.string.file_is_too_large));
    }
    void fileNotLoadedException(){
        postMessageException(activity.getString(R.string.fail_to_load_file));
    }
    void creatingListException(){
        postMessageException(activity.getString(R.string.fail_to_create_list));
    }
    void postMessageException(String msg){
        activity.handler.post(() -> {
            Toast.makeText(activity,msg, Toast.LENGTH_SHORT).show();
            activity.loadingFinished(false);
        });
    }
}
