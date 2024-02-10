package com.sjapps.jsonlist;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sjapps.logs.CrashLogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class FileSystem {

    final static String LogFile = "Log.json";
    final static String StateFile = "CheckState.json";

    public static JsonObject loadDataToJsonObj(JsonElement data){
        return data.getAsJsonObject();
    }
    public static JsonArray loadDataToJsonArray(JsonElement data) {
        return data.getAsJsonArray();
    }

    public static String LoadDataFromFile(Context context, Uri uri, InputStream   inputStream, AssetFileDescriptor fileDescriptor) {

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
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            long currentBytes = 0;
            long fileSize = fileDescriptor.getLength();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                currentBytes += line.length();
                ((MainActivity) context).updateProgress((int)((currentBytes/(float)fileSize)*100));
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

    static String LoadData(Context context,String FileName){
        File file = new File(context.getFilesDir(), FileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileReader fileReader = new FileReader(file.getAbsolutePath());


            StringBuilder builder = new StringBuilder();
            String jsonString = null;
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((jsonString = bufferedReader.readLine()) != null) {
                builder.append(jsonString);
            }
            bufferedReader.close();
            if (builder.toString().equals("") || builder.toString().equals("{}"))
                return null;

            return new String(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void SaveData(Context context, String FileName, String data){
        File file = new File(context.getFilesDir(), FileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(data);
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static AppState loadStateData(Context context) {
        String data = LoadData(context, StateFile);
        if (data == null)
            return new AppState();
        return new Gson().fromJson(data, AppState.class);
    }

    public static CrashLogs loadLogData(Context context){
        String data = LoadData(context,LogFile);
        if (data == null)
            return new CrashLogs();
        return new Gson().fromJson(data,CrashLogs.class);
    }

    public static void SaveState(Context context, String data) {
        SaveData(context,StateFile,data);
    }

    public static void SaveLog(Context context, String data) {
        SaveData(context,LogFile,data);
    }

    public static File createTempFile(Context context, String data, String fileName) {
        File file = new File(context.getCacheDir(),fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(data);
            fileWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return file;
    }

    public static boolean deleteTempFile(Context context, String fileName){
        File file = new File(context.getCacheDir(),fileName);
        return file.delete();
    }
}
