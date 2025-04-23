package com.sj14apps.jsonlist.core;

import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

public class IsArrayOfObjectsTest {

    @Test
    public void testIsArrayOfObjects_true() {

        JsonArray array = new JsonArray();
        JsonObject object = new JsonObject();
        object.addProperty("test","val");

        array.add(object);

        assertTrue(JsonFunctions.isArrayOfObjects(array));
    }

    @Test
    public void testIsArrayOfObjects_true2() {

        JsonArray array = new JsonArray();
        JsonObject object = new JsonObject();
        object.addProperty("test","val");
        object.addProperty("test2",true);

        JsonObject object2 = new JsonObject();
        object.addProperty("test","val");
        object.addProperty("test2",2);

        array.add(object);
        array.add(object2);

        assertTrue(JsonFunctions.isArrayOfObjects(array));
    }

    @Test
    public void testIsArrayOfObjects_true3() {

        JsonArray array = new JsonArray();
        JsonObject object = new JsonObject();
        JsonObject object2 = new JsonObject();
        object.addProperty("test","val");
        object.addProperty("test2",2);
        object.add("obj", object2);

        array.add(object);

        assertTrue(JsonFunctions.isArrayOfObjects(array));
    }

    @Test
    public void testIsArrayOfObjects_true4() {

        String data = "[{'test':'val'}]";
        String data2 = "[{'test':'val'},{'test2':true}]";

        JsonElement element = JsonParser.parseString(data);
        JsonArray array = JsonFunctions.loadDataToJsonArray(element);

        JsonElement element2 = JsonParser.parseString(data2);
        JsonArray array2 = JsonFunctions.loadDataToJsonArray(element2);

        assertTrue(JsonFunctions.isArrayOfObjects(array));
        assertTrue(JsonFunctions.isArrayOfObjects(array2));
    }

    @Test
    public void testIsArrayOfObjects_false() {

        JsonArray array = new JsonArray();
        array.add(1);
        array.add("test");
        array.add(true);

        assertFalse(JsonFunctions.isArrayOfObjects(array));
    }


    @Test
    public void testIsArrayOfObjects_false2() {

        JsonArray array = new JsonArray();
        array.add(1);
        JsonObject object = new JsonObject();
        object.addProperty("test",123);
        array.add(object);
        array.add("test");
        array.add(true);

        assertFalse(JsonFunctions.isArrayOfObjects(array));
    }

    @Test
    public void testIsArrayOfObjects_false3() {

        String data = "[{'test':'val'},123]";
        String data2 = "[false,{'test':'val'},{'test2':true}]";

        JsonElement element = JsonParser.parseString(data);
        JsonArray array = JsonFunctions.loadDataToJsonArray(element);

        JsonElement element2 = JsonParser.parseString(data2);
        JsonArray array2 = JsonFunctions.loadDataToJsonArray(element2);

        assertFalse(JsonFunctions.isArrayOfObjects(array));
        assertFalse(JsonFunctions.isArrayOfObjects(array2));
    }

}