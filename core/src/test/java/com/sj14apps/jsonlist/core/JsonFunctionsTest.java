package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.ArrayList;

public class JsonFunctionsTest {

    @Test
    public void testGetJsonArrayRoot() {
        // Create a sample JSON array
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject());

        JsonNode result = JsonFunctions.getJsonArrayRoot(jsonArray);
        assertNotNull(result);
        assertTrue(result.isArray);
        assertTrue(result.isRoot);
        assertEquals(JsonNode.ARRAY_OBJECTS_NAME, result.key);
        assertEquals(1, result.children.size());
    }

    @Test
    public void testGetJsonArray() {
        // Create a sample JSON array with JSON objects
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject());
        jsonArray.add(new JsonObject());

        ArrayList<JsonNode> result = JsonFunctions.getJsonArray(null,jsonArray);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testIsArrayOfObjects() {
        // Create a sample JSON array with JSON objects
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonObject());
        jsonArray.add(new JsonObject());

        boolean result = JsonFunctions.isArrayOfObjects(jsonArray);
        assertTrue(result);
    }

    @Test
    public void testGetJsonObject() {
        // Create a sample JSON object
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key1", "value1");
        jsonObject.addProperty("key2", "value2");

        JsonNode result = JsonFunctions.getJsonObject(jsonObject);
        assertNotNull(result);
        assertEquals(2, result.children.size());
    }

    @Test
    public void testGetStringFromJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("test1", "value1\nvalue2");
        jsonObject.addProperty("test2", "value1\tvalue2");
        jsonObject.addProperty("test3", "value1\rvalue2");
        jsonObject.addProperty("test4", "value1\bvalue2");
        jsonObject.addProperty("test5", "value1\fvalue2");
        jsonObject.addProperty("test6", "value1\"value2");
        jsonObject.addProperty("test7", "value1\\value2");

        JsonNode result = JsonFunctions.getJsonObject(jsonObject);
        assertNotNull(result);
        assertEquals("value1\nvalue2", result.children.get(0).value);
        assertEquals("value1\tvalue2", result.children.get(1).value);
        assertEquals("value1\rvalue2", result.children.get(2).value);
        assertEquals("value1\bvalue2", result.children.get(3).value);
        assertEquals("value1\fvalue2", result.children.get(4).value);
        assertEquals("value1\"value2", result.children.get(5).value);
        assertEquals("value1\\value2", result.children.get(6).value);
    }

}
