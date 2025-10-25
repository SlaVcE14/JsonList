package com.sj14apps.jsonlist.core;

public class SearchItem {
    public String value;
    public String path;
    public int id;
    public int arrayId;

    public SearchItem(){}
    public SearchItem(String value, String path){
        this(value,path,-1);
    }

    public SearchItem(String value, String path,int id){
        this(value,path,id,-1);
    }

    public SearchItem(String value, String path,int id,int arrayId){
        this.value = value;
        this.path = path;
        this.id = id;
        this.arrayId = arrayId;
    }
}
