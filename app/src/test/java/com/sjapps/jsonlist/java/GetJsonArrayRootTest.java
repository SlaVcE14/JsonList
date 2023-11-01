package com.sjapps.jsonlist.java;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sjapps.jsonlist.FileSystem;

import org.junit.Test;

import java.util.ArrayList;

public class GetJsonArrayRootTest {

    @Test
    public void getJsonArrayRootTest(){
        String data = "[ {data2:\"123\"} ]";
        JsonElement element = JsonParser.parseString(data);

        JsonArray array = FileSystem.loadDataToJsonArray(element);

        ArrayList<ListItem> itemsArr = JsonFunctions.getJsonArrayRoot(array,e -> {});
        ArrayList<ListItem> expectedArr = new ArrayList<>();

        ListItem root = new ListItem();
        root.setName("Json Array");
        root.setIsArray(true);

        ArrayList<ListItem> listItems = new ArrayList<>();

        ListItem OLItem = new ListItem();

        OLItem.setName("data2");
        OLItem.setValue("123");
        listItems.add(OLItem);

        ArrayList<ArrayList<ListItem>> arrayListArrayList = new ArrayList<>();
        arrayListArrayList.add(listItems);
        root.setListObjects(arrayListArrayList);
        expectedArr.add(root);
        assertEquals(expectedArr,itemsArr);

    }
    @Test
    public void getJsonArrayRootTest2(){
        String data = "[{\"data2\":123},1242,true,null]";
        JsonElement element = JsonParser.parseString(data);

        JsonArray array = FileSystem.loadDataToJsonArray(element);

        ArrayList<ListItem> itemsArr = JsonFunctions.getJsonArrayRoot(array,e -> {});


        ArrayList<ListItem> expectedArr = new ArrayList<>();

        ListItem root = new ListItem();
        root.setName("Array items");
        root.setIsArray(true);
        root.setValue("[{\"data2\":123},1242,true,null]");

        expectedArr.add(root);
        assertEquals(expectedArr,itemsArr);

    }

    @Test
    public void testGetJsonArrayRootWithEmptyArray() {
        JsonArray jsonArray = new JsonArray();
        ArrayList<ListItem> result = JsonFunctions.getJsonArrayRoot(jsonArray, e -> {});
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Json Array", result.get(0).getName());
        assertTrue(result.get(0).isArray());
        assertEquals(0, result.get(0).getListObjects().size());
    }

}
