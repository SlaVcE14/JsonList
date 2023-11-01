package com.sjapps.jsonlist.java;

import static junit.framework.TestCase.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import org.junit.Test;

import java.util.ArrayList;

public class GetJsonObjectTest {

    @Test
    public void testGetJsonObjectWithNestedObject() {
        JsonObject jsonObject = new JsonObject();
        JsonObject nestedObject = new JsonObject();
        nestedObject.addProperty("nestedKey", "nestedValue");
        jsonObject.add("key1", nestedObject);

        ArrayList<ListItem> result = JsonFunctions.getJsonObject(jsonObject, e -> {});

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("key1", result.get(0).getName());
        assertTrue(result.get(0).isObject());
        assertEquals(1, result.get(0).getObjects().size());
        assertEquals("nestedKey", result.get(0).getObjects().get(0).getName());
        assertEquals("nestedValue", result.get(0).getObjects().get(0).getValue());
    }

    @Test
    public void testGetJsonObjectWithNestedArray() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("value1");
        jsonArray.add("value2");
        jsonObject.add("key1", jsonArray);

        ArrayList<ListItem> result = JsonFunctions.getJsonObject(jsonObject, e -> {});

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("key1", result.get(0).getName());
        assertTrue(result.get(0).isArray());
        assertEquals("value1",result.get(0).getListObjects().get(0).get(0).getValue());
        assertEquals("value2",result.get(0).getListObjects().get(1).get(0).getValue());

    }


    @Test
    public void testGetJsonObjectFromString() {
        String jsonString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

        ArrayList<ListItem> result = JsonFunctions.getJsonObject(jsonObject, e -> {});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("key1", result.get(0).getName());
        assertEquals("value1", result.get(0).getValue());
        assertEquals("key2", result.get(1).getName());
        assertEquals("value2", result.get(1).getValue());
    }


    @Test
    public void getJsonObjectFromString() {
        String data = "{'item1':123,'item2':'test','item3':true,'item4':null,'item5':'123'}";

        JsonObject object =  new Gson().fromJson(data, JsonObject.class);

        ArrayList<ListItem> items = JsonFunctions.getJsonObject(object,e -> {});

        ArrayList<ListItem> expected = new ArrayList<>();

        ListItem item1 = new ListItem();
        ListItem item2 = new ListItem();
        ListItem item3 = new ListItem();
        ListItem item4 = new ListItem();
        ListItem item5 = new ListItem();

        item1.setName("item1");
        item1.setValue("123");

        item2.setName("item2");
        item2.setValue("test");


        item3.setName("item3");
        item3.setValue("true");

        item4.setName("item4");
        item4.setValue("null");

        item5.setName("item5");
        item5.setValue("123");

        expected.add(item1);
        expected.add(item2);
        expected.add(item3);
        expected.add(item4);
        expected.add(item5);


        assertNotNull(items);
        assertEquals(item1,items.get(0));
        assertEquals(item2,items.get(1));
        assertEquals(item3,items.get(2));
        assertEquals(item4,items.get(3));
        assertEquals(item5,items.get(4));
        assertEquals(expected, items);

    }

    @Test
    public void getJsonObjectFromStringObject() {
        String data = "{'item1':'test','item2':{'item3':true,'item4':null}}";

        JsonObject object =  new Gson().fromJson(data, JsonObject.class);

        ArrayList<ListItem> items = JsonFunctions.getJsonObject(object,e -> {});

        ArrayList<ListItem> expected = new ArrayList<>();

        ListItem item1 = new ListItem();
        ListItem item2 = new ListItem();

        item1.setName("item1");
        item1.setValue("test");

        item2.setName("item2");
        item2.setIsObject(true);

        ArrayList<ListItem> objs = new ArrayList<>();
        ListItem item3 = new ListItem();
        item3.setName("item3");
        item3.setValue("true");
        objs.add(item3);

        ListItem item4 = new ListItem();
        item4.setName("item4");
        item4.setValue("null");
        objs.add(item4);

        item2.setObjects(objs);

        expected.add(item1);
        expected.add(item2);

        assertNotNull(items);
        assertEquals(item1,items.get(0));
        assertEquals(item2,items.get(1));
        assertTrue(items.get(1).isObject());
        assertEquals(objs,items.get(1).getObjects());
        assertEquals(item3,items.get(1).getObjects().get(0));
        assertEquals(item4,items.get(1).getObjects().get(1));
        assertEquals(expected, items);

    }

    @Test
    public void getJsonObjectFromStringArray() {
        String data = "{'item1':'test','item2':[{'item3':true},{'item4':null}]}";

        JsonObject object =  new Gson().fromJson(data, JsonObject.class);

        ArrayList<ListItem> items = JsonFunctions.getJsonObject(object,e -> {});

        ArrayList<ListItem> expected = new ArrayList<>();

        ListItem item1 = new ListItem();
        ListItem item2 = new ListItem();

        item1.setName("item1");
        item1.setValue("test");

        item2.setName("item2");
        item2.setIsArray(true);

        ArrayList<ArrayList<ListItem>> objs = new ArrayList<>();
        ArrayList<ListItem> items1 = new ArrayList<>();
        ListItem item3 = new ListItem();
        item3.setName("item3");
        item3.setValue("true");
        items1.add(item3);
        objs.add(items1);


        ArrayList<ListItem> items2 = new ArrayList<>();
        ListItem item4 = new ListItem();
        item4.setName("item4");
        item4.setValue("null");
        items2.add(item4);
        objs.add(items2);

        item2.setListObjects(objs);

        expected.add(item1);
        expected.add(item2);

        assertNotNull(items);
        assertEquals(item1,items.get(0));
        assertEquals(item2,items.get(1));
        assertTrue(items.get(1).isArray());
        assertEquals(items1,items.get(1).getListObjects().get(0));
        assertEquals(items2,items.get(1).getListObjects().get(1));
        assertEquals(expected, items);

    }
}
