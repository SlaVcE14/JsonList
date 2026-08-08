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

    public static JsonNode getJsonArrayRoot(JsonArray array) {
        JsonNode mainNode = new JsonNode().root().array();
        setArrayName(array, mainNode);
        mainNode.setChildren(getJsonArray(mainNode, array));
        return mainNode;
    }

    public static ArrayList<JsonNode> getJsonArray(JsonNode parentNode, JsonArray array) {
        ArrayList<JsonNode> ArrList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonNode childNode;
            JsonElement element = array.get(i);

            if (element instanceof JsonObject) {
                childNode = getJsonObject(parentNode, (JsonObject) element);
            } else if (element instanceof JsonArray) {
                childNode = new JsonNode().root().array();
                ArrayList<JsonNode> ListOfItems = getJsonArray(childNode, (JsonArray) element);
                childNode.setChildren(ListOfItems);
                setArrayName((JsonArray) element, childNode);
            } else {
                childNode = new JsonNode();
                childNode.setValue(getStringFromJson(element.toString()));
            }
            
            childNode.setId(i);
            childNode.setParent(parentNode);
            ArrList.add(childNode);
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

    public static JsonNode getJsonObject(JsonNode parentNode, JsonObject obj) {
        JsonNode mainNode = new JsonNode().object();
        Set<String> keys = obj.keySet();
        Object[] keysArray = keys.toArray();

        for (Object o : keysArray) {
            JsonNode item = setItem(obj, o);
            item.setKey(o.toString());
            item.setParent(mainNode);
            mainNode.children.add(item);
        }
        return mainNode;
    }

    private static void setArrayName(JsonArray array, JsonNode item) {
        if (isArrayOfObjects(array)) {
            item.setKey(JsonNode.ARRAY_OBJECTS_NAME);
            return;
        }
        if (isArrayOfArray(array)) {
            item.setKey(JsonNode.ARRAY_NAME);
            return;
        }
        item.setKey(JsonNode.ARRAY_ITEMS_NAME);
    }

    private static String getStringFromJson(String value) {
        String ret = value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length() - 1) : value;
        return ret
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static JsonNode setItem(JsonObject obj, Object o) {
        if (obj.get(o.toString()) instanceof JsonObject) {
            return getJsonObject(null, (JsonObject) obj.get(o.toString()));
        }
        if (obj.get(o.toString()) instanceof JsonArray) {
            JsonArray array = (JsonArray) obj.get(o.toString());
            JsonNode item = new JsonNode().array();
            item.setChildren(getJsonArray(item, array));
            return item;
        }
        JsonNode item = new JsonNode();
        item.setValue(getStringFromJson(obj.get(o.toString()).toString()));
        return item;
    }

    static ArrayList<ListItem> getArrayList(ArrayList<JsonNode> list) {
        ArrayList<ListItem> newList = new ArrayList<>();
        ListItem space = new ListItem().Space();
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isObject) {
                newList.add(new ListItem(list.get(i)));
                continue;
            }
            for (JsonNode node : list.get(i).children) {
                newList.add(new ListItem(node));
            }
            newList.add(space);
        }
        return newList;
    }

    static ArrayList<ListItem> getObject(JsonNode obj) {
        ArrayList<ListItem> items = new ArrayList<>();

        for (JsonNode node : obj.children) {
            items.add(new ListItem(node));
        }

        return items;
    }

    public static ArrayList<ListItem> getListFromNode(JsonNode node) {
        if (node.isArray) {
            return getArrayList(node.children);
        }
        return getObject(node);
    }

    public static String getAsPrettyPrint(String data) {
        JsonElement json = JsonParser.parseString(data);
        Gson gson = new Gson().newBuilder().setPrettyPrinting().serializeNulls().create();
        return gson.toJson(json);
    }

    public static String convertToRawString(JsonNode rootNode) {
        return convertToRawString(rootNode, true);
    }

    public static String convertToRawString(JsonNode rootNode, boolean prettyPrint) {
        JsonElement rootElement;
        rootElement = convertJsonNodeToElement(rootNode);
        GsonBuilder builder = new GsonBuilder().serializeNulls();
        if (prettyPrint)
            builder.setPrettyPrinting();
        return builder.create().toJson(rootElement);
    }

    private static JsonElement convertJsonNodeToElement(JsonNode item) {
        if (item.isArray) {
            JsonArray jsonArray = new JsonArray();
            for (JsonNode subItem : item.children) {
                if (!subItem.isArray && !subItem.isObject) {
                    if (subItem.key != null) {
                        JsonObject obj = new JsonObject();
                        obj.add(subItem.key, convertJsonNodeToElement(subItem));
                        jsonArray.add(obj);
                        continue;
                    }

                    jsonArray.add(getPrimitive(subItem));
                    continue;
                }

                if (subItem.isArray) {
                    jsonArray.add(convertJsonNodeToElement(subItem));
                    continue;
                }

                JsonObject obj = new JsonObject();
                for (JsonNode subitem : subItem.children) {
                    obj.add(subitem.key, convertJsonNodeToElement(subitem));
                }
                jsonArray.add(obj);
            }
            return jsonArray;
        }

        if (item.isObject) {
            JsonObject jsonObject = new JsonObject();
            for (JsonNode subitem : item.children) {
                jsonObject.add(subitem.key, convertJsonNodeToElement(subitem));
            }
            return jsonObject;
        }

        return getPrimitive(item);

    }

    private static JsonElement getPrimitive(JsonNode item) {

        String val = item.value;

        if (val == null) return new JsonPrimitive("");
        if ("null".equals(val)) return JsonNull.INSTANCE;
        if ("true".equals(val)) return new JsonPrimitive(true);
        if ("false".equals(val)) return new JsonPrimitive(false);

        if (hasInvalidLeadingZero(val)) return new JsonPrimitive(val);
          
        try { return new JsonPrimitive(Long.parseLong(val)); } catch (NumberFormatException ignored) {}
        try { return new JsonPrimitive(Double.parseDouble(val)); } catch (NumberFormatException ignored) {}

        return new JsonPrimitive(item.value);

    }

    private static boolean hasInvalidLeadingZero(String val) {
        if (val.length() < 2) return false;

        if (val.charAt(0) == '-') {
            return val.length() > 2 && val.charAt(1) == '0' && Character.isDigit(val.charAt(2));
        }

        if (val.charAt(0) == '0') {
            char next = val.charAt(1);
            return Character.isDigit(next);
        }
        return false;
    }

    public static ArrayList<SearchItem> searchItem(JsonData data, String val) {
        ArrayList<SearchItem> searchItems = new ArrayList<>();
        JsonNode root = data.getRootNode();
        searchItem(root.children, searchItems, "", val.toLowerCase(), data.searchMode);
        return searchItems;
    }

    public static void searchItem(ArrayList<JsonNode> nodes, ArrayList<SearchItem> searchItems, String path, String val, int searchMode){
        int index = 0;
        for (JsonNode node : nodes) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            if (searchMode != 2 && node.key != null && node.key.toLowerCase().contains(val))
                searchItems.add(new SearchItem(node, node.key, path, index));

            if (node.isArray || node.isObject) {
                if (node.isObject) {
                    searchItem(
                            node.children,
                            searchItems,
                            path + (path.equals("") ? "" : "///") + (node.key != null ? node.key : node.id),
                            val,
                            searchMode
                    );
                    index++;
                    continue;
                }

                searchItem(
                        node.children,
                        searchItems,
                        path + (path.equals("") ? "" : "///") + (node.id != null ? node.id + "///" : "") + node.key,
                        val,
                        searchMode
                );
                index++;
                continue;
            }

            if (searchMode != 1 && node.value.toLowerCase().contains(val))
                searchItems.add(new SearchItem(
                        node,
                        (node.key != null ? node.key + ": " : "") + node.value,
                        path,
                        index
                ));
            index++;
        }

    }

    public static JsonNode getNodeFromPath(JsonNode rootNode, Path path) {
        if (path == null)
            return rootNode;
        JsonNode current = rootNode;
        for (PathSegment segment : path.pathSegments) {
            current = getChildren(current, segment);
        }
        return current;
    }

    public static JsonNode getChildren(JsonNode parent, PathSegment segment) {
        if (parent == null || parent.children == null)
            return parent;
        for (JsonNode node : parent.children) {
            if (segment.isId && node.id != null && node.id == Integer.parseInt(segment.val))
                return node;
            if (node.key != null && node.key.equals(segment.val))
                return node;
        }
        return parent;
    }
}
