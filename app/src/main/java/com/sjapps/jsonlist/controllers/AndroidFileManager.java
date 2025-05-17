package com.sjapps.jsonlist.controllers;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sj14apps.jsonlist.core.controllers.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.OpenableColumns;

public class AndroidFileManager implements FileManager {

    private final MainActivity activity;
    private final Handler handler;

    public AndroidFileManager(MainActivity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }


    @Override
    public void importFromFile() {
        if ((activity.readFileThread != null && activity.readFileThread.isAlive()) || activity.isUrlSearching) {
            Snackbar.make(activity.getWindow().getDecorView(), R.string.loading_file_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }
        if (activity.isEditMode){
            Snackbar.make(activity.getWindow().getDecorView(), R.string.editing_in_progress, BaseTransientBottomBar.LENGTH_SHORT).show();
            return;
        }
        if (activity.state == null)
            activity.LoadStateData();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(activity.state.isMIMEFilterDisabled()?"*/*" : Build.VERSION.SDK_INT > Build.VERSION_CODES.P?"application/json":"application/*");
        activity.ActivityResultData.launch(intent);
    }

    @Override
    public void readFile(InputStream inputStream,String fileName, long fileSize, FileCallback callback) {
        try {
            byte[] buffer = new byte[4096];
            StringBuilder data = new StringBuilder();
            int bytesRead;
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                data.append(new String(buffer, 0, bytesRead));

                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                    callback.onProgressUpdate(progress);
                }
            }

            callback.onFileLoaded(data.toString(), fileName);
        } catch (IOException e) {
            handler.post(callback::onFileLoadFailed);
        }
    }

    @Override
    public void writeFile(OutputStream outputStream, String data, FileWriteCallback callback) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            int totalSize = dataBytes.length;
            int bufferSize = 4096;
            int totalWritten = 0;

            for (int i = 0; i < totalSize; i += bufferSize) {
                int length = Math.min(bufferSize, totalSize - i);
                outputStream.write(dataBytes, i, length);
                totalWritten += length;

                int progress = ((totalWritten * 100) / totalSize);
                callback.onProgressUpdate(progress);
            }

            outputStream.flush();
            outputStream.close();
            callback.onFileWriteSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            handler.post(callback::onFileWriteFail);
        }
    }

    @Override
    public void saveFile(String fileName) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fileName == null || fileName.isEmpty()? "unsaved.json" : fileName);
        activity.ActivityResultSaveData.launch(intent);
    }

    public void validatePath(Uri uri){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String path = uri.getPath();
            if (path.contains("../"))
                throw new SecurityException();
            Path normalized = java.nio.file.FileSystems.getDefault().getPath(path).normalize();
            if (normalized.startsWith("/data"))
                throw new SecurityException();
        }
    }

    public static String getFileName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver()
                .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        return null;
    }
}
