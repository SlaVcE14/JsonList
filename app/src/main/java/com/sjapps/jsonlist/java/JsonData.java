package com.sjapps.jsonlist.java;

import java.util.ArrayList;

public class JsonData {
    String path = "";
    ArrayList<ListItem> rootList = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ArrayList<ListItem> getRootList() {
        return rootList;
    }

    public void setRootList(ArrayList<ListItem> rootList) {
        this.rootList = rootList;
    }

    public boolean isEmptyPath(){
        return path.equals("");
    }
    public void clearPath(){
        path = "";
    }
    public String[] splitPath(){
        return path.split("///");
    }

    public boolean isRootListNull(){
        return rootList == null;
    }


    public static String getName(String str){
        if (str.startsWith("{") && str.contains("}") && str.substring(1, str.indexOf("}")).matches("^[0-9]+"))
            return str.substring(str.indexOf("}")+1);
        return str;
    }
}
