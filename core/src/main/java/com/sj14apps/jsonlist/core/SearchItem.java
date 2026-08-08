package com.sj14apps.jsonlist.core;

public class SearchItem {
    public String value;
    public String path;
    public int id;
    public JsonNode node;

    public SearchItem(){}
    public SearchItem(JsonNode node,String value, String path){
        this(node,value,path,-1);
    }

    public SearchItem(JsonNode node,String value, String path,int id){
        this.value = value;
        this.path = path;
        this.id = id;
        this.node = node;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "value='" + value + '\'' +
                ", path='" + path + '\'' +
                ", id=" + id +
                '}';
    }
}
