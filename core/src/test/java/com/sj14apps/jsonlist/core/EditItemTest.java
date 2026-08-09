
package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.ArrayList;

public class EditItemTest {

    private void assertJsonEqual(String resultJson, String expectedJson) {
        System.out.println("expected: " + expectedJson);
        System.out.println("result: " + resultJson);
        System.out.println("----------");
        JsonElement result = JsonParser.parseString(resultJson);
        JsonElement expected = JsonParser.parseString(expectedJson);
        assertEquals("Edited JSON did not match with the expected result", expected, result);
    }

    @Test
    public void EditValue(){
        System.out.println("EditValue");
        String input = "{\"name\":\"John Doe\",\"age\": 30}";
        String expected = "{\"name\":\"John Smith\",\"age\": 30}";

        JsonNode rootList = JsonFunctions.getJsonObject(JsonParser.parseString(input).getAsJsonObject());
        rootList.children.get(0).setValue("John Smith");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList,false),expected);
    }

    @Test
    public void EditName(){
        System.out.println("EditName");
        String input = "{\"name\":\"John Doe\",\"age\": 30}";
        String expected = "{\"Full Name\":\"John Doe\",\"age\": 30}";

        JsonNode rootList = JsonFunctions.getJsonObject(JsonParser.parseString(input).getAsJsonObject());
        rootList.children.get(0).setKey("Full Name");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList,false),expected);
    }

    @Test
    public void EditArrayPrimitives(){
        System.out.println("EditArrayPrimitives");
        String input = "[\"test\", 30, true]";
        String expected = "[\"test123\", 35, false]";

        JsonNode rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        ArrayList<JsonNode> items = rootList.children;
        items.get(0).setValue("test123");
        items.get(1).setValue("35");
        items.get(2).setValue("false");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList,false),expected);
    }

    @Test
    public void EditNumbers(){
        System.out.println("EditNumbers");
        String input = "[1, 2.2, -3, -4.4]";
        String expected = "[\"01\", \"02.2\", \"-03\", \"-04.4\"]";

        JsonNode rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        ArrayList<JsonNode> items = rootList.children;
        items.get(0).setValue("01");
        items.get(1).setValue("02.2");
        items.get(2).setValue("-03");
        items.get(3).setValue("-04.4");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList,false),expected);
    }

    @Test
    public void EditRootArrayName(){
        System.out.println("EditRootArrayName");
        String input = "[[false],\"test\", 30, true]";
        String expected = "[[false],\"test\", 30, true]";

        JsonNode rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        rootList.children.get(0).setKey("Test");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList,false),expected);
    }

    @Test
    public void EditRootArrayName2(){
        System.out.println("EditRootArrayName2");
        String input = "[[[\"aa\",12,true,{\"test\":\"test123\",\"val\":123},213]]]";
        String expected = "[[[\"aa\",12,true,{\"test\":\"test123\",\"val\":123},213]]]";

        JsonNode rootList = JsonFunctions.getJsonArrayRoot(JsonParser.parseString(input).getAsJsonArray());
        rootList.children.get(0).setKey("Edit");
        assertJsonEqual(JsonFunctions.convertToRawString(rootList,false),expected);
    }

}

