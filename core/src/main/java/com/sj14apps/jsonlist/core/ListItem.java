package com.sj14apps.jsonlist.core;

import java.util.ArrayList;

public class ListItem {

    JsonNode jsonNode;
    private boolean isSpace;
    private int position = -1;

    public ListItem(){
    }

    public ListItem(JsonNode node){
        this.jsonNode = node;
    }


    public String getName() {
        if (isSpace)
            return null;
        return jsonNode.key;
    }

    public void setName(String name) {
        if (!jsonNode.isRoot)
            jsonNode.setKey(name);
    }

    public String getValue() {
        return jsonNode.value;
    }

    public void setValue(String value) {
        jsonNode.setValue(value);
    }

    public boolean isArray() {
        if (jsonNode == null)
            return false;
        return jsonNode.isArray;
    }

    public void setIsArray(boolean array) {
        jsonNode.isArray = array;
    }

    public boolean isObject() {
        if (jsonNode == null)
            return false;
        return jsonNode.isObject;
    }

    @Deprecated
    public void setIsObject(boolean object) {
        jsonNode.isObject = object;
    }

    public boolean isSpace() {
        return isSpace;
    }

    public boolean isRootItem() {
        return jsonNode.isRoot;
    }

    @Deprecated
    public void setIsRootItem(boolean b) {
        jsonNode.isRoot = b;
    }

    public void setIsSpace(boolean space) {
        isSpace = space;
    }

    public ArrayList<ListItem> getObjects() {
        if (!jsonNode.isObject) return null;

        ArrayList<ListItem> listItems = new ArrayList<>();
        for (JsonNode node: jsonNode.children) {
            ListItem item = new ListItem(node);
            listItems.add(item);
        }

        return listItems;
    }

    public JsonNode getJsonNode(){
        return this.jsonNode;
    }
    public void setJsonNode(JsonNode jsonNode){
        this.jsonNode = jsonNode;
    }

    public int getId() {
        return jsonNode == null? -1: jsonNode.id == null? -1: jsonNode.id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


    @Override
    public String toString() {
        return "{" +
                "\"Position\":" + position +
                ", \"isSpace\":" + isSpace +
                ", \"node\":" + jsonNode +

                '}';
    }

    public ListItem Space() {
        setIsSpace(true);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListItem listItem = (ListItem) o;
        if (isSpace != listItem.isSpace) return false;
        if (position != listItem.position) return false;
        return java.util.Objects.equals(jsonNode, listItem.jsonNode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(jsonNode, isSpace, position);
    }

    public static ListItem error() {
        JsonNode node = new JsonNode();
        node.setKey("ERROR");
        node.setValue("This was not supposed to happen!!");
        return new ListItem(node);
    }
}
