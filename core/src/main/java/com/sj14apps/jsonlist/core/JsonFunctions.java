package com.sj14apps.jsonlist.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
            item.setParentList(mainList);
            setItem(obj,o,item);
            mainList.add(item);
        }
        return mainList;
    }

    private static void setArrayName(JsonArray array, ListItem item){
        if(isArrayOfObjects(array)) {
            item.setName("Objects Array");
            item.setIsRootItem(true);
            return;
        }
        if (isArrayOfArray(array)){
            item.setName("Array");
            item.setIsRootItem(true);
            return;
        }
        item.setName("Array items");
        item.setIsRootItem(true);
    }
    private static String getStringFromJson(String value){
        String ret = value.startsWith("\"") && value.endsWith("\"") ? value.substring(1,value.length()-1) : value;
        return ret
                .replace("\\n","\n")
                .replace("\\t","\t")
                .replace("\\r","\r")
                .replace("\\b","\b")
                .replace("\\f","\f")
                .replace("\\\"","\"")
                .replace("\\\\","\\");
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


    public static String convertToRawString(ArrayList<ListItem> rootList) {
        JsonElement rootElement;

        if (rootList.size() == 1 && rootList.get(0).isArray() &&
                (
                        rootList.get(0).getName().equals("Array items") ||
                                rootList.get(0).getName().equals("Objects Array") ||
                                rootList.get(0).getName().equals("Array"))
        ) {

            rootElement = convertListItemToElement(rootList.get(0));

        } else {
            JsonObject jsonObject = new JsonObject();
            for (ListItem item : rootList) {
                jsonObject.add(item.getName(), convertListItemToElement(item));
            }
            rootElement = jsonObject;
        }

        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(rootElement);
    }


    private static JsonElement convertListItemToElement(ListItem item) {
        if (item.isArray()) {
            JsonArray jsonArray = new JsonArray();
            for (ArrayList<ListItem> sublist : item.getListObjects()) {
                if (sublist.size() == 1 && !sublist.get(0).isArray() && !sublist.get(0).isObject()) {
                    if (sublist.get(0).getName() != null){
                        JsonObject obj = new JsonObject();
                        obj.add(sublist.get(0).getName(),convertListItemToElement(sublist.get(0)));
                        jsonArray.add(obj);
                        continue;
                    }

                    jsonArray.add(getPrimitive(sublist.get(0)));
                    continue;
                }
                if (sublist.size() == 1 && sublist.get(0).isArray()) {
                    jsonArray.add(convertListItemToElement(sublist.get(0)));
                    continue;
                }

                JsonObject obj = new JsonObject();
                for (ListItem subitem : sublist) {
                    obj.add(subitem.getName(), convertListItemToElement(subitem));
                }
                jsonArray.add(obj);
            }
            return jsonArray;
        }

        if (item.isObject()) {
            JsonObject jsonObject = new JsonObject();
            for (ListItem subitem : item.getObjects()) {
                jsonObject.add(subitem.getName(), convertListItemToElement(subitem));
            }
            return jsonObject;
        }

        return getPrimitive(item);

    }

    private static JsonElement getPrimitive(ListItem item){

        String val = item.getValue();

        if (val == null) return new JsonPrimitive("");
        if (val.equals("null")) return JsonNull.INSTANCE;
        if (val.equals("true")) return new JsonPrimitive(true);
        if (val.equals("false")) return new JsonPrimitive(false);
        if (val.matches("^\\d+\\.\\d+$")) return new JsonPrimitive(Double.parseDouble(item.getValue()));
        if (val.matches("^\\d+$")) return new JsonPrimitive(Long.parseLong(item.getValue()));

        return new JsonPrimitive(item.getValue());

    }


    public static ArrayList<SearchItem> searchItem(JsonData data, String val){
        ArrayList<SearchItem> searchItems = new ArrayList<>();

        ArrayList<ListItem> root = data.getRootList();
        searchItem(root,searchItems,"",val,data.searchMode,0,-1);
        return searchItems;
    }

    public static void searchItem(ArrayList<ListItem> list,ArrayList<SearchItem> searchItems, String path, String val,int searchMode,int currentID,int arrayId){

        for (ListItem item : list){
            if (searchMode != 2 && item.getName() != null && item.getName().toLowerCase().contains(val)){
                searchItems.add(new SearchItem(item.getName(),path,currentID,arrayId));
            }
            if (item.isObject()){
                searchItem(item.getObjects(),
                        searchItems,
                        path + (path.equals("") ? "": "///" + (item.getId()!=-1?"{" + item.getId() + "}":"")) + item.getName(),
                        val,
                        searchMode,
                        0,
                        -1
                );
                currentID++;
                continue;
            }
            if (item.isArray()){
                int idInArray = 0;
                int arrayNum = 0;
                for (ArrayList<ListItem> listItems : item.getListObjects()){
                    searchItem(listItems,
                            searchItems,
                            path + (path.equals("") ? "": "///" + (item.getId()!=-1?"{" + item.getId() + "}":"")) + item.getName(),
                            val,
                            searchMode,
                            idInArray,
                            arrayNum
                    );
                    idInArray += listItems.size()+1;
                    arrayNum++;
                }
                currentID++;
                continue;
            }
            if (searchMode != 1 && item.getValue() != null && item.getValue().toLowerCase().contains(val)){
                searchItems.add(new SearchItem((item.getName() != null?item.getName() + ": " :"") + item.getValue(),path,currentID,arrayId));
            }
            currentID++;
        }

    }

}
