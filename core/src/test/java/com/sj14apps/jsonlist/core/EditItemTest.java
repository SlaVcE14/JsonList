package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.ArrayList;

public class EditItemTest {

    private void assertJsonEqual(String resultJson, String expectedJson) {
        JsonElement result = JsonParser.parseString(resultJson);
        JsonElement expected = JsonParser.parseString(expectedJson);
        assertEquals("Edited JSON did not match with the expected result", expected, result);
    }

    @Test
    public void EditValue(){
        String input = "{\"name\":\"John Doe\",\"age\": 30}";
        String expected = "{\"name\":\"John Smith\",\"age\": 30}";

        ArrayList<ListItem> rootList = JsonFunctions.getJsonObject(JsonParser.parseString(input).getAsJsonObject());
        rootList.get(0).setValue("John Smith");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList),expected);
    }

    @Test
    public void EditName(){
        String input = "{\"name\":\"John Doe\",\"age\": 30}";
        String expected = "{\"Full Name\":\"John Doe\",\"age\": 30}";

        ArrayList<ListItem> rootList = JsonFunctions.getJsonObject(JsonParser.parseString(input).getAsJsonObject());
        rootList.get(0).setName("Full Name");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList),expected);
    }

    @Test
    public void EditArrayPrimitives(){
        String input = "[\"test\", 30, true]";
        String expected = "[\"test123\", 35, false]";

        ArrayList<ListItem> rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        ArrayList<ArrayList<ListItem>> items = rootList.get(0).getListObjects();
        items.get(0).get(0).setValue("test123");
        items.get(1).get(0).setValue("35");
        items.get(2).get(0).setValue("false");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList),expected);
    }

    @Test
    public void EditRootArrayName(){
        String input = "[\"test\", 30, true]";
        String expected = "[\"test\", 30, true]";

        ArrayList<ListItem> rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        rootList.get(0).setName("Test");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList),expected);
    }

    @Test
    public void EditRootArrayName2(){
        String input = "[[[\"aa\",12,true,{\"test\":\"test123\",\"val\":123},213]]]";
        String expected = "[[[\"aa\",12,true,{\"test\":\"test123\",\"val\":123},213]]]";

        ArrayList<ListItem> rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        System.out.println(rootList.get(0).getName());
        rootList.get(0).setName("Edit");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList),expected);
    }

}
