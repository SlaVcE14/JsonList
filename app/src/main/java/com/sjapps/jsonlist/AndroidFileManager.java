package com.sjapps.jsonlist;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.sjapps.jsonlist.core.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

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
        if (activity.state == null)
            activity.LoadStateData();

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(activity.state.isMIMEFilterDisabled()?"*/*" : Build.VERSION.SDK_INT > Build.VERSION_CODES.P?"application/json":"application/*");
        activity.ActivityResultData.launch(intent);
    }

    @Override
    public void readFile(InputStream inputStream, long fileSize, FileCallback callback) {
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

            callback.onFileLoaded(data.toString());
        } catch (IOException e) {
            handler.post(callback::onFileLoadFailed);
        }
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
}
