package com.sj14apps.jsonlist.core.controllers;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileManager {

    void importFromFile();
    void readFile(InputStream inputStream,String fileName, long fileSize, FileCallback callback);
    void writeFile(OutputStream outputStream, String data, FileWriteCallback callback);

    void saveFile(String fileName);

    interface FileCallback {
        void onFileLoaded(String data, String fileName);
        void onFileLoadFailed();
        void onProgressUpdate(int progress);
    }

    interface FileWriteCallback {
        void onFileWriteSuccess();
        void onFileWriteFail();
        void onProgressUpdate(int progress);
    }
}
