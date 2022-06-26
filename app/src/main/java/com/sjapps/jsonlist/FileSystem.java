package com.sjapps.jsonlist;

import android.net.Uri;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
public class FileSystem {


    public static JsonObject loadDataToJsonObj(JsonElement data){
        return data.getAsJsonObject();
    }
    public static JsonArray loadDataToJsonArray(JsonElement data) {
        return data.getAsJsonArray();
    }

    public static String LoadDataFromFile(MainActivity mainActivity, Uri uri) {

        StringBuilder sb = new StringBuilder();
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = (FileInputStream) mainActivity.getContentResolver().openInputStream(uri);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                sb.append(line);
            }

            if (sc.ioException() != null) {
                throw sc.ioException();
            }


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }
            return sb.toString();

        }
    }
}
