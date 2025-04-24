package com.sj14apps.jsonlist.core;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import org.junit.Test;

import java.util.ArrayList;

public class GetArrayListTest {

    @Test
    public void testGetArrayList() {
        ArrayList<ArrayList<ListItem>> inputList = new ArrayList<>();

        ArrayList<ListItem> list1 = new ArrayList<>();
        ListItem item1 = new ListItem();
        item1.setName("Item 1");
        item1.setValue("Value 1");
        list1.add(item1);

        ArrayList<ListItem> list2 = new ArrayList<>();
        ListItem item2 = new ListItem();
        item2.setName("Item 2");
        item2.setValue("Value 2");
        list2.add(item2);

        inputList.add(list1);
        inputList.add(list2);

        ArrayList<ListItem> result = JsonFunctions.getArrayList(inputList);

        assertEquals(4, result.size());
        assertEquals("Item 1", result.get(0).getName());
        assertEquals("Value 1", result.get(0).getValue());
        assertFalse(result.get(0).isSpace());
        assertEquals("Item 2", result.get(2).getName());
        assertEquals("Value 2", result.get(2).getValue());
        assertFalse(result.get(2).isSpace());
        assertEquals(0, result.get(0).getId());
        assertEquals(1, result.get(2).getId());
    }

    @Test
    public void testGetArrayListEmpty() {

        ArrayList<ArrayList<ListItem>> lists = new ArrayList<>();
        ArrayList<ListItem> result = JsonFunctions.getArrayList(lists);

        assertNotNull(result);
        assertEquals(0,result.size());
    }

}
