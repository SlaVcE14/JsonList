package com.sj14apps.jsonlist.core.controllers;

public interface JsonLoader {

    //TODO get a file name form other place???
    void LoadData(String Data, String fileName, JsonLoaderCallback callBack);

    interface JsonLoaderCallback {
        void start();
        void started();
        void failed();
        void success();
        void after();
    }

}
