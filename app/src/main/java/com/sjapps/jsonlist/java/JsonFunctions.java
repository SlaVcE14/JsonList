package com.sjapps.jsonlist.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Set;

public class JsonFunctions {

    public static ArrayList<ListItem> getJsonArrayRoot(JsonArray array, ExceptionCallback callback) {
        ArrayList<ListItem> mainList = new ArrayList<>();
        ListItem item = new ListItem();
        item.setName("Json Array");
        item.setIsArrayOfObjects(true);
        item.setListObjects(getJsonArray(array,callback));
        mainList.add(item);

        return mainList;
    }

    public static ArrayList<ArrayList<ListItem>> getJsonArray(JsonArray array, ExceptionCallback callback) {
        ArrayList<ArrayList<ListItem>> ArrList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) instanceof JsonObject) {
                ArrayList<ListItem> ListOfItems = getJsonObject((JsonObject) array.get(i),callback);
                ArrList.add(ListOfItems);
            }
        }
        return ArrList;
    }

    static boolean isArrayOfObjects(JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            if (!(array.get(i) instanceof JsonObject)) {
                return false;
            }
        }
        return true;
    }

    public static ArrayList<ListItem> getJsonObject(JsonObject obj, ExceptionCallback callback) {
        ArrayList<ListItem> mainList = new ArrayList<>();
        Set<String> keys = obj.keySet();
        Object[] keysArray = keys.toArray();

        try {
            for (Object o : keysArray) {
                ListItem item = new ListItem();
                item.setName(o.toString());
                setItem(obj,o,item,callback);
                mainList.add(item);
            }
        } catch (Exception e) {
            callback.onException(e);
            return null;
        }
        return mainList;
    }

    private static void setItem(JsonObject obj, Object o, ListItem item, ExceptionCallback callback){
        if (obj.get(o.toString()) instanceof JsonObject) {
            item.setIsObject(true);
            ArrayList<ListItem> objList = getJsonObject((JsonObject) obj.get(o.toString()), callback);
            item.setObjects(objList);
            return;
        }
        if (obj.get(o.toString()) instanceof JsonArray) {
            JsonArray array = (JsonArray) obj.get(o.toString());
            if (isArrayOfObjects(array)) {
                item.setIsArrayOfObjects(true);
                ArrayList<ArrayList<ListItem>> ArrList = getJsonArray(array,callback);
                item.setListObjects(ArrList);
                return;
            }
            item.setIsArray(true);
            item.setValue(array.toString());
            return;
        }
        item.setValue(obj.get(o.toString()).toString());
    }

    static ArrayList<ListItem> getArrayList(ArrayList<ArrayList<ListItem>> list) {
        ArrayList<ListItem> newList = new ArrayList<>();
        for (ArrayList<ListItem> lists : list) {
            setId(lists, list.indexOf(lists));
            newList.addAll(lists);
            newList.add(new ListItem().Space());
        }
        return newList;
    }

    private static void setId(ArrayList<ListItem> lists, int id) {

        for (ListItem listItem : lists) {
            listItem.setId(id);
        }
    }

    public static ArrayList<ListItem> getListFromPath(String path, ArrayList<ListItem> rootList) {


        String[] pathStrings = path.split("///");

        ArrayList<ListItem> list = rootList;

        for (String pathString : pathStrings) {

            int id = -1;

            if (pathString.startsWith("{") && pathString.contains("}") && pathString.substring(1, pathString.indexOf("}")).matches("^[0-9]+")) {
                id = Integer.parseInt(pathString.substring(1, pathString.indexOf("}")));
            }

            for (ListItem item : list) {
                if (item.getName() == null || !item.getName().equals(id != -1 ? pathString.substring(pathString.indexOf("}") + 1) : pathString))
                    continue;

                if (id != -1 && item.getId() != id)
                    continue;

                if (item.isArrayOfObjects()) {
                    list = getArrayList(item.getListObjects());
                    break;
                }
                list = list.get(list.indexOf(item)).getObjects();
                if (list == null)
                    list = new ArrayList<>();
                break;
            }
        }
        return list;

    }
}
