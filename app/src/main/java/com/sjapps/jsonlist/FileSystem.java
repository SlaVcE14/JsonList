package com.sjapps.jsonlist;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
public class FileSystem {


    public static JsonObject loadDataToJsonObj(JsonElement data){
        return data.getAsJsonObject();
    }
    public static JsonArray loadDataToJsonArray(JsonElement data) {
        return data.getAsJsonArray();
    }

    public static String LoadDataFromFile(MainActivity mainActivity, Uri uri) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String path = uri.getPath();
            if (path.contains("../"))
                throw new SecurityException();
            Path normalized = java.nio.file.FileSystems.getDefault().getPath(path).normalize();
            if (normalized.startsWith("/data"))
                throw new SecurityException();
        }

        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream   inputStream = (FileInputStream) mainActivity.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            AssetFileDescriptor fileDescriptor = mainActivity.getContentResolver().openAssetFileDescriptor(uri , "r");

            long currentBytes = 0;
            long fileSize = fileDescriptor.getLength();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                currentBytes += line.length();
                mainActivity.updateProgress((int)((currentBytes/(float)fileSize)*100));
            }

            fileDescriptor.close();
            inputStream.close();
            reader.close();

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
