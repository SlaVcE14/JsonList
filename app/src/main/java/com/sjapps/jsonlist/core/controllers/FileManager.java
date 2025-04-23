package com.sjapps.jsonlist.core.controllers;

import java.io.InputStream;

public interface FileManager {

    void importFromFile();
    void readFile(InputStream inputStream,long fileSize, FileCallback callback);

    interface FileCallback {
        void onFileLoaded(String data);
        void onFileLoadFailed();
        void onProgressUpdate(int progress);
    }
}
