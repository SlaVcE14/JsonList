package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Optional;

public class GetArrayListTest {

    @Test
    public void testGetArrayList() {
        ArrayList<JsonNode> inputList = new ArrayList<>();

        JsonNode list1 = new JsonNode().object();
        JsonNode item1 = new JsonNode();
        item1.setKey("Item 1");
        item1.setValue("Value 1");
        item1.setParent(list1);
        list1.children.add(item1);
        list1.setId(0);

        JsonNode list2 = new JsonNode().object();
        JsonNode item2 = new JsonNode();
        item2.setKey("Item 2");
        item2.setValue("Value 2");
        item2.setParent(list2);
        list2.children.add(item2);
        list2.setId(1);

        inputList.add(list1);
        inputList.add(list2);

        ArrayList<ListItem> result = JsonFunctions.getArrayList(inputList);

        System.out.println(result);
        assertEquals(4, result.size());
        assertEquals("Item 1", result.get(0).getName());
        assertEquals("Value 1", result.get(0).getValue());
        assertFalse(result.get(0).isSpace());
        assertEquals("Item 2", result.get(2).getName());
        assertEquals("Value 2", result.get(2).getValue());
        assertFalse(result.get(2).isSpace());
        assertEquals(Optional.of(0), Optional.ofNullable(result.get(0).jsonNode.parent.id));
        assertEquals(Optional.of(1), Optional.ofNullable(result.get(2).jsonNode.parent.id));
    }

    @Test
    public void testGetArrayListEmpty() {

        ArrayList<JsonNode> lists = new ArrayList<>();
        ArrayList<ListItem> result = JsonFunctions.getArrayList(lists);

        assertNotNull(result);
        assertEquals(0,result.size());
    }

}
