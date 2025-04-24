package com.sj14apps.jsonlist.core.controllers;

public interface JsonLoader {

    void LoadData(String Data, JsonLoaderCallback callBack);

    interface JsonLoaderCallback {
        void start();
        void started();
        void failed();
        void success();
        void after();
    }

}
