package com.sj14apps.jsonlist.core;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Set;

public class JsonFunctions {

    public static ArrayList<ListItem> getJsonArrayRoot(JsonArray array) {
        ArrayList<ListItem> mainList = new ArrayList<>();
        ListItem item = new ListItem();
        setArrayName(array,item);
        item.setIsArray(true);
        item.setListObjects(getJsonArray(array));
        mainList.add(item);
        return mainList;
    }

    public static ArrayList<ArrayList<ListItem>> getJsonArray(JsonArray array) {
        ArrayList<ArrayList<ListItem>> ArrList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i) instanceof JsonObject) {
                ArrayList<ListItem> ListOfItems = getJsonObject((JsonObject) array.get(i));
                ArrList.add(ListOfItems);
                continue;
            }
            if (array.get(i) instanceof JsonArray){

                ArrayList<ArrayList<ListItem>> ListOfItems = getJsonArray((JsonArray) array.get(i));

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
            item.setValue(getStringFromJson(array.get(i).toString()));
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

    public static ArrayList<ListItem> getJsonObject(JsonObject obj) {
        ArrayList<ListItem> mainList = new ArrayList<>();
        Set<String> keys = obj.keySet();
        Object[] keysArray = keys.toArray();

        for (Object o : keysArray) {
            ListItem item = new ListItem();
            item.setName(o.toString());
            setItem(obj,o,item);
            mainList.add(item);
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
    private static String getStringFromJson(String value){
        return value.startsWith("\"") && value.endsWith("\"") ? value.substring(1,value.length()-1) : value;
    }

    private static void setItem(JsonObject obj, Object o, ListItem item){
        if (obj.get(o.toString()) instanceof JsonObject) {
            item.setIsObject(true);
            ArrayList<ListItem> objList = getJsonObject((JsonObject) obj.get(o.toString()));
            item.setObjects(objList);
            return;
        }
        if (obj.get(o.toString()) instanceof JsonArray) {
            JsonArray array = (JsonArray) obj.get(o.toString());

            item.setIsArray(true);
            item.setListObjects(getJsonArray(array));
            return;
        }
        item.setValue(getStringFromJson(obj.get(o.toString()).toString()));
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

    public static String getAsPrettyPrint(String data){
        JsonElement json = JsonParser.parseString(data);
        Gson gson = new Gson().newBuilder().setPrettyPrinting().serializeNulls().create();
        return gson.toJson(json);
    }

    public static JsonObject loadDataToJsonObj(JsonElement data){
        return data.getAsJsonObject();
    }
    public static JsonArray loadDataToJsonArray(JsonElement data) {
        return data.getAsJsonArray();
    }
}
