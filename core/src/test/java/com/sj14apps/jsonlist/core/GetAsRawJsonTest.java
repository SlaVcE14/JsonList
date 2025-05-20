package com.sj14apps.jsonlist.core;

import com.google.gson.*;
import static junit.framework.TestCase.assertEquals;
import java.util.ArrayList;


import org.junit.Test;

public class GetAsRawJsonTest {

    private void assertJsonEqual(String inputJson) {
        JsonElement input = JsonParser.parseString(inputJson);

        ArrayList<ListItem> rootList;
        if (input.isJsonArray()) {
            rootList = JsonFunctions.getJsonArrayRoot(input.getAsJsonArray());
        } else if (input.isJsonObject()) {
            rootList = JsonFunctions.getJsonObject(input.getAsJsonObject());
        } else {
            throw new IllegalArgumentException("Unsupported JSON root type.");
        }

        String resultJson = JsonFunctions.convertToRawString(rootList);

        JsonElement result = JsonParser.parseString(resultJson);
        assertEquals("Re-converted JSON did not match original structure",input, result);
    }

    @Test
    public void testPrimitives() {
        assertJsonEqual("{\"a\":123,\"b\":true,\"c\":false,\"d\":null,\"e\":\"text\"}");
    }

    @Test
    public void testNestedObject() {
        assertJsonEqual("{\"user\":{\"name\":\"Alice\",\"info\":{\"active\":true}}}");
    }

    @Test
    public void testArrayOfPrimitives() {
        assertJsonEqual("{\"values\":[1,2,3]}");
    }

    @Test
    public void testArrayOfObjects() {
        assertJsonEqual("{\"users\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}");
    }

    @Test
    public void testArrayOfArrays() {
        assertJsonEqual("{\"matrix\":[[1,2],[3,4]]}");
    }

    @Test
    public void testMixedArray() {
        assertJsonEqual("{\"mixed\":[1,\"two\",true,null,{\"x\":1},[2,3]]}");
    }

    @Test
    public void testRootArray() {
        assertJsonEqual("[{\"id\":1},{\"id\":2}]");
    }

    @Test
    public void testEmptyStructures() {
        assertJsonEqual("{\"emptyArray\":[],\"emptyObject\":{}}");
    }

    @Test
    public void testComplexStructure() {
        assertJsonEqual("{\"data\":[{\"id\":1,\"info\":{\"valid\":true}},2,null,\"str\"]}");
    }
    @Test
    public void testBigJson(){
        assertJsonEqual(" { \"name\": \"John Doe\", \"age\": 30, \"city\": \"New York\", " +
                "\"hobbies\": [\"reading\", \"hiking\", \"music\"], \"occupation\": { \"title\": " +
                "\"Software Engineer\", \"company\": \"Acme Inc.\", \"experience\": 5}, \"contact\":" +
                " { \"email\": \"john.doe@example.com\", \"phone\": \"+1 212-555-1234\", \"website\":" +
                " \"https://johndoe.com\"},\"additional_info\": {\"languages\": [\"English\", " +
                "\"Spanish (intermediate)\"],\"interests\": [\"artificial intelligence\"," +
                " \"machine learning\", \"photography\"],\"education\":" +
                " [{\"institution\": \"University of California, Berkeley\",\"degree\":" +
                " \"Bachelor of Science in Computer Science\",\"graduation_year\": 2015}]}}");
    }
}
