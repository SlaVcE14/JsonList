package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import org.junit.Test;

public class GetJsonObjectTest {

    @Test
    public void testGetJsonObjectWithNestedObject() {
        JsonObject jsonObject = new JsonObject();
        JsonObject nestedObject = new JsonObject();
        nestedObject.addProperty("nestedKey", "nestedValue");
        jsonObject.add("key1", nestedObject);

        JsonNode result = JsonFunctions.getJsonObject(jsonObject);

        assertNotNull(result);
        assertEquals(1, result.children.size());
        assertEquals("key1", result.children.get(0).key);
        assertTrue(result.children.get(0).isObject);
        assertEquals(1, result.children.get(0).children.size());
        assertEquals("nestedKey", result.children.get(0).children.get(0).key);
        assertEquals("nestedValue", result.children.get(0).children.get(0).value);
    }

    @Test
    public void testGetJsonObjectWithNestedArray() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add("value1");
        jsonArray.add("value2");
        jsonObject.add("key1", jsonArray);

        JsonNode result = JsonFunctions.getJsonObject(jsonObject);

        assertNotNull(result);
        assertEquals(1, result.children.size());
        assertEquals("key1", result.children.get(0).key);
        assertTrue(result.children.get(0).isArray);
        assertEquals("value1",result.children.get(0).children.get(0).value);
        assertEquals("value2",result.children.get(0).children.get(1).value);

    }


    @Test
    public void testGetJsonObjectFromString() {
        String jsonString = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        JsonObject jsonObject = new Gson().fromJson(jsonString, JsonObject.class);

        JsonNode result = JsonFunctions.getJsonObject(jsonObject);

        assertNotNull(result);
        assertEquals(2, result.children.size());
        assertEquals("key1", result.children.get(0).key);
        assertEquals("value1", result.children.get(0).value);
        assertEquals("key2", result.children.get(1).key);
        assertEquals("value2", result.children.get(1).value);
    }


    @Test
    public void getJsonObjectFromString() {
        String data = "{'item1':123,'item2':'test','item3':true,'item4':null,'item5':'123'}";

        JsonObject object =  new Gson().fromJson(data, JsonObject.class);

        JsonNode items = JsonFunctions.getJsonObject(object);

        JsonNode expected = new JsonNode().object();

        JsonNode item1 = new JsonNode();
        JsonNode item2 = new JsonNode();
        JsonNode item3 = new JsonNode();
        JsonNode item4 = new JsonNode();
        JsonNode item5 = new JsonNode();

        item1.setKey("item1");
        item1.setValue("123");
        item1.setParent(expected);

        item2.setKey("item2");
        item2.setValue("test");
        item2.setParent(expected);


        item3.setKey("item3");
        item3.setValue("true");
        item3.setParent(expected);

        item4.setKey("item4");
        item4.setValue("null");
        item4.setParent(expected);

        item5.setKey("item5");
        item5.setValue("123");
        item5.setParent(expected);

        expected.children.add(item1);
        expected.children.add(item2);
        expected.children.add(item3);
        expected.children.add(item4);
        expected.children.add(item5);


        assertNotNull(items);
        assertEquals(item1,items.children.get(0));
        assertEquals(item2,items.children.get(1));
        assertEquals(item3,items.children.get(2));
        assertEquals(item4,items.children.get(3));
        assertEquals(item5,items.children.get(4));
        assertEquals(expected, items);

    }

    @Test
    public void getJsonObjectFromStringObject() {
        String data = "{'item1':'test','item2':{'item3':true,'item4':null}}";

        JsonObject object =  new Gson().fromJson(data, JsonObject.class);

        JsonNode items = JsonFunctions.getJsonObject(object);

        JsonNode expected = new JsonNode().object();

        JsonNode item1 = new JsonNode();
        JsonNode item2 = new JsonNode().object();

        item1.setKey("item1");
        item1.setValue("test");
        item1.setParent(expected);

        item2.setKey("item2");
        item2.setParent(expected);

        JsonNode item3 = new JsonNode();
        item3.setKey("item3");
        item3.setValue("true");
        item3.setParent(item2);
        item2.children.add(item3);

        JsonNode item4 = new JsonNode();
        item4.setKey("item4");
        item4.setValue("null");
        item4.setParent(item2);
        item2.children.add(item4);

        expected.children.add(item1);
        expected.children.add(item2);

        assertNotNull(items);
        assertEquals(item1,items.children.get(0));
        assertEquals(item2,items.children.get(1));
        assertTrue(items.children.get(1).isObject);
        assertEquals(item2.children,items.children.get(1).children);
        assertEquals(item3,items.children.get(1).children.get(0));
        assertEquals(item4,items.children.get(1).children.get(1));
        assertEquals(expected, items);

    }

    @Test
    public void getJsonObjectFromStringArray() {
        String data = "{'item1':'test','item2':[{'item3':true},{'item4':null}]}";

        JsonObject object =  new Gson().fromJson(data, JsonObject.class);

        JsonNode items = JsonFunctions.getJsonObject(object);

        JsonNode expected = new JsonNode().object();

        JsonNode item1 = new JsonNode();
        JsonNode item2 = new JsonNode().array();

        item1.setKey("item1");
        item1.setValue("test");

        item2.setKey("item2");

        item1.setParent(expected);
        item2.setParent(expected);

//        ArrayList<ArrayList<ListItem>> objs = new ArrayList<>();
        JsonNode items1 = new JsonNode().object();
        JsonNode item3 = new JsonNode();
        item3.setKey("item3");
        item3.setValue("true");
        item3.setParent(item2);
        items1.children.add(item3);
        item2.children.add(items1);


        JsonNode items2 = new JsonNode().object();
        JsonNode item4 = new JsonNode();
        item4.setKey("item4");
        item4.setValue("null");
        item4.setParent(item2);
        items2.children.add(item4);
        item2.children.add(items2);

        items1.setId(0);
        items2.setId(1);
        items1.setParent(item2);
        items2.setParent(item2);

        expected.children.add(item1);
        expected.children.add(item2);

        assertNotNull(items);
        assertEquals(item1,items.children.get(0));
        assertEquals(item2,items.children.get(1));
        assertTrue(items.children.get(1).isArray);
        assertEquals(items1,items.children.get(1).children.get(0));
        assertEquals(items2, items.children.get(1).children.get(1));
        assertEquals(expected, items);

    }
}
