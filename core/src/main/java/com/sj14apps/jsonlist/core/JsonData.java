package com.sj14apps.jsonlist.core;

import java.util.ArrayList;
import java.util.Stack;

public class JsonData {
    Path path = new Path();
    JsonNode rootNode;
    JsonNode currentNode;
    Stack<Integer> previousPosStack = new Stack<>();
    String rawData = "";
    String fileName;

    int previousPos = -1;
    public int searchMode = 0;

    public String getPathAsString() {
        return path.toString();
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public ArrayList<ListItem> getRootList() {
        if (rootNode.isObject)
            return JsonFunctions.getObject(rootNode);
        return JsonFunctions.getArrayList(rootNode.children);
    }

    public ArrayList<ListItem> getCurrentList() {
        if (currentNode.isObject)
            return JsonFunctions.getObject(currentNode);
        if (currentNode.isArray)
            return JsonFunctions.getArrayList(currentNode.children);
        ArrayList<ListItem> items = new ArrayList<>();
        items.add(ListItem.error());
        return items;
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(JsonNode rootNode) {
        this.rootNode = rootNode;
    }

    public JsonNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(JsonNode currentNode) {
        this.currentNode = currentNode;
    }

    public void setRawData(String data) {
        this.rawData = data;
    }

    public String getRawData() {
        return rawData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isEmptyPath() {
        return path.isEmpty();
    }

    public void clearPath() {
        path = new Path();
    }


    public boolean isRootNodeNull() {
        return rootNode == null;
    }

    public void goBack() {

        if (!previousPosStack.isEmpty())
            previousPos = previousPosStack.pop();

        if (path.isEmpty())
            return;

        path.goBack();

    }

    public void addPreviousPos(int pos) {
        previousPosStack.push(pos);
    }

    public int getPreviousPos() {
        return previousPos;
    }

    public void clearPreviousPos() {
        previousPosStack.clear();
    }


}
