package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.ArrayList;

public class GetJsonArrayRootTest {

    @Test
    public void getJsonArrayRootTest(){
        String data = "[ {data2:\"123\"} ]";
        JsonElement element = JsonParser.parseString(data);

        JsonArray array = element.getAsJsonArray();

        JsonNode itemsArr = JsonFunctions.getJsonArrayRoot(array);
        JsonNode expectedArr = new JsonNode().root().array();
        expectedArr.setKey(JsonNode.ARRAY_OBJECTS_NAME);

        JsonNode listItems = new JsonNode().object();
        listItems.setId(0);

        JsonNode OLItem = new JsonNode();

        OLItem.setKey("data2");
        OLItem.setValue("123");
        OLItem.setParent(expectedArr);
        listItems.children.add(OLItem);
        listItems.setParent(expectedArr);

        expectedArr.children.add(listItems);

        assertEquals(expectedArr,itemsArr);

    }

    @Test
    public void getJsonArrayRootTest2(){
        String data = "[{\"data2\":123},1242,true,null]";
        JsonElement element = JsonParser.parseString(data);

        JsonArray array = element.getAsJsonArray();

        JsonNode itemsArr = JsonFunctions.getJsonArrayRoot(array);


        JsonNode expectedRootArr = new JsonNode().root().array();

        expectedRootArr.setKey(JsonNode.ARRAY_ITEMS_NAME);

        ArrayList<JsonNode> rootChildren = new ArrayList<>();

        JsonNode items1 = new JsonNode().object();
        JsonNode item1 = new JsonNode();
        item1.setKey("data2");
        item1.setValue("123");
        items1.setParent(items1);
        items1.children.add(item1);
        items1.setId(0);
        items1.setParent(expectedRootArr);
        rootChildren.add(items1);

        JsonNode item2 = new JsonNode();
        item2.setValue("1242");
        item2.setId(1);
        item2.setParent(expectedRootArr);
        rootChildren.add(item2);

        JsonNode item3 = new JsonNode();
        item3.setValue("true");
        item3.setId(2);
        item3.setParent(expectedRootArr);
        rootChildren.add(item3);

        JsonNode item4 = new JsonNode();
        item4.setValue("null");
        item4.setId(3);
        item4.setParent(expectedRootArr);
        rootChildren.add(item4);


        expectedRootArr.setChildren(rootChildren);

        assertEquals(expectedRootArr,itemsArr);

    }

    @Test
    public void testGetJsonArrayRootWithEmptyArray() {
        JsonArray jsonArray = new JsonArray();
        JsonNode result = JsonFunctions.getJsonArrayRoot(jsonArray);
        assertNotNull(result);
        ArrayList<ListItem> list = JsonFunctions.getListFromNode(result);
        assertEquals(0, list.size());
        assertEquals(JsonNode.ARRAY_OBJECTS_NAME, result.key);
        assertTrue(result.isArray);
        assertEquals(0, result.children.size());
    }

}
