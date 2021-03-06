package com.sjapps.jsonlist;

import java.util.ArrayList;

public class ListItem {

    private String Name;
    private String Value;
    private boolean isArrayOfObjects;
    private boolean isArray;
    private boolean isObject;
    private boolean isSpace;
    private ArrayList<ListItem> Objects;
    private ArrayList<ArrayList<ListItem>> ListObjects;

    public ListItem(){
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public boolean isArrayOfObjects() {
        return isArrayOfObjects;
    }

    public void setIsArrayOfObjects(boolean array) {
        isArrayOfObjects = array;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setIsArray(boolean array) {
        isArray = array;
    }

    public boolean isObject() {
        return isObject;
    }

    public void setIsObject(boolean object) {
        isObject = object;
    }

    public boolean isSpace() {
        return isSpace;
    }

    public void setIsSpace(boolean space) {
        isSpace = space;
    }

    public ArrayList<ListItem> getObjects() {
        return Objects;
    }

    public void setObjects(ArrayList<ListItem> objects) {
        Objects = objects;
    }

    public ArrayList<ArrayList<ListItem>> getListObjects() {
        return ListObjects;
    }

    public void setListObjects(ArrayList<ArrayList<ListItem>> listObjects) {
        ListObjects = listObjects;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "Name='" + Name + '\'' +
                ", Value='" + Value + '\'' +
                ", isArrayOfObjects=" + isArrayOfObjects +
                ", isArray=" + isArray +
                ", isObject=" + isObject +
                ", isSpace=" + isSpace +
                ", Objects=" + Objects +
                ", ListObjects=" + ListObjects +
                '}';
    }

    public ListItem Space() {
        setIsSpace(true);
        return this;
    }
}
