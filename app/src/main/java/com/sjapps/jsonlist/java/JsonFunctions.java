package com.sjapps.jsonlist.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Set;

public class JsonFunctions {

    public static ArrayList<ListItem> getJsonArrayRoot(JsonArray array, ExceptionCallback callback) {
        ArrayList<ListItem> mainList = new ArrayList<>();
        ListItem item = new ListItem();
        setArrayName(array,item);
        item.setIsArray(true);
        item.setListObjects(getJsonArray(array, callback));
        mainList.add(item);
        return mainList;
    }

    public static ArrayList<ArrayList<ListItem>> getJsonArray(JsonArray array, ExceptionCallback callback) {
        ArrayList<ArrayList<ListItem>> ArrList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) instanceof JsonObject) {
                ArrayList<ListItem> ListOfItems = getJsonObject((JsonObject) array.get(i),callback);
                ArrList.add(ListOfItems);
                continue;
            }
            if (array.get(i) instanceof JsonArray){

                ArrayList<ArrayList<ListItem>> ListOfItems = getJsonArray((JsonArray) array.get(i),callback);

                ArrayList<ListItem> itemsInList = new ArrayList<>();
                ListItem arrItem = new ListItem();

                setArrayName((JsonArray) array.get(i),arrItem);
                arrItem.setIsArray(true);
                arrItem.setListObjects(ListOfItems);

                itemsInList.add(arrItem);
                ArrList.add(itemsInList);
                continue;
            }
            ListItem item = new ListItem();
            String value = array.get(i).toString();
            item.setValue(value.startsWith("\"") && value.endsWith("\"") ? value.substring(1,value.length()-1) : value);
            ArrayList<ListItem> items = new ArrayList<>();
            items.add(item);
            ArrList.add(items);
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

    static boolean isArrayOfArray(JsonArray array) {
        for (int i = 0; i < array.size(); i++) {
            if (!(array.get(i) instanceof JsonArray)) {
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

    private static void setArrayName(JsonArray array, ListItem item){
        if(isArrayOfObjects(array)) {
            item.setName("Objects Array");
            return;
        }
        if (isArrayOfArray(array)){
            item.setName("Array");
            return;
        }
        item.setName("Array items");
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

            item.setIsArray(true);
            item.setListObjects(getJsonArray(array,callback));
            return;
        }
        String value = obj.get(o.toString()).toString();
        item.setValue(value.startsWith("\"") && value.endsWith("\"") ? value.substring(1,value.length()-1) : value);
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

                if (item.isArray()) {
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
