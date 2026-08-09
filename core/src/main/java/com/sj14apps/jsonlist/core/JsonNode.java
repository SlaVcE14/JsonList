package com.sj14apps.jsonlist.core;

import java.util.ArrayList;
import java.util.Objects;

public class JsonNode {
    public static final String ARRAY_NAME = "[...]";
    public static final String ARRAY_ITEMS_NAME = "[...]";
    public static final String ARRAY_OBJECTS_NAME = "[...]";
    public String key;
    public String value;
    public JsonNode parent;
    public Integer id;
    public boolean isObject;
    public boolean isArray;
    public boolean isRoot;
    public ArrayList<JsonNode> children;

    public JsonNode root() {
        isRoot = true;
        return this;
    }

    public JsonNode object() {
        isObject = true;
        children = new ArrayList<>();
        return this;
    }

    public JsonNode array() {
        isArray = true;
        children = new ArrayList<>();
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                "," + getKeyToString() +
                "," + getValueToString() +
                ",\"isObject\":" + isObject +
                ",\"isArray\":" + isArray +
                ",\"isRoot\":" + isRoot +
                ",\"children\":" + children +
                ",\"parent\": " + getParentToString() +
                '}';
    }

    public String getKeyToString(){
        return "\"key\":" + (key!=null && !key.startsWith("\"")?"\"":"") +  key + (key!=null && !key.startsWith("\"")?"\"":"");
    }
    public String getValueToString(){
        return "\"value\":" + (value!=null && !value.startsWith("\"")?"\"":"") + value + (value!=null && !value.startsWith("\"")?"\"":"");
    }

    public String getParentToString(){
        return (parent== null? null: "{ \"id\":" + parent.id + "," + parent.getKeyToString() + "}");
    }


    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setParent(JsonNode parent) {
        this.parent = parent;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setChildren(ArrayList<JsonNode> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonNode)) return false;

        JsonNode node = (JsonNode) o;

        int size1 = children == null ? 0 : children.size();
        int size2 = node.children == null ? 0 : node.children.size();

        return isObject == node.isObject &&
                isArray == node.isArray &&
                isRoot == node.isRoot &&
                size1 == size2 &&
                Objects.equals(key, node.key) &&
                Objects.equals(value, node.value) &&
                Objects.equals(id, node.id) &&
                Objects.equals(getParentToString(), node.getParentToString());
    }

    @Override
    public int hashCode() {
        int size = children == null ? 0 : children.size();

        return Objects.hash(
                key,
                value,
                getParentToString(),
                id,
                isObject,
                isArray,
                isRoot,
                size
        );
    }
}
