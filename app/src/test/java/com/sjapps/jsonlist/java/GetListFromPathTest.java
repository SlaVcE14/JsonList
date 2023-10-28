package com.sjapps.jsonlist.java;

import static junit.framework.TestCase.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.util.ArrayList;

public class GetListFromPathTest {

    @Test
    public void testGetListFromPathWithNestedStructureObject() {
        ArrayList<ListItem> rootList = new ArrayList<>();
        ListItem item1 = new ListItem();
        item1.setName("Item1");
        rootList.add(item1);
        item1.setIsObject(true);

        ArrayList<ListItem> nestedList = new ArrayList<>();
        ListItem item2 = new ListItem();
        item2.setName("Item2");

        ArrayList<ListItem> nestedNestedList = new ArrayList<>();

        ListItem item3 = new ListItem();
        item3.setName("Item3");
        item3.setValue("test");

        ListItem item4 = new ListItem();
        item4.setName("Item4");
        item4.setValue("123");

        nestedNestedList.add(item3);
        nestedNestedList.add(item4);

        item2.setObjects(nestedNestedList);
        item2.setIsObject(true);


        nestedList.add(item2);
        item1.setObjects(nestedList);

        ArrayList<ListItem> result = JsonFunctions.getListFromPath("Item1", rootList);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Item2", result.get(0).getName());

        result = JsonFunctions.getListFromPath("Item1///Item2", rootList);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Item3", result.get(0).getName());
        assertEquals("test", result.get(0).getValue());
        assertEquals("Item4", result.get(1).getName());
        assertEquals("123", result.get(1).getValue());
    }

    @Test
    public void testGetListFromPathWithNestedStructureArrayOfObjects() {
        ArrayList<ListItem> rootList = new ArrayList<>();
        ListItem item1 = new ListItem();
        item1.setName("Item1");
        rootList.add(item1);
        item1.setIsArrayOfObjects(true);

        ArrayList<ArrayList<ListItem>> nestedListList = new ArrayList<>();
        ArrayList<ListItem> nestedList1 = new ArrayList<>();
        ListItem item2 = new ListItem();
        item2.setName("Item2");
        item2.setValue("test");
        nestedList1.add(item2);

        ArrayList<ListItem> nestedList2 = new ArrayList<>();
        ListItem item3 = new ListItem();
        item3.setName("Item3");

        ArrayList<ListItem> innerListItem3 = new ArrayList<>();

        ListItem item = new ListItem();
        item.setName("item");
        item.setValue("val");

        innerListItem3.add(item);
        item3.setObjects(innerListItem3);
        nestedList2.add(item3);

        nestedListList.add(nestedList1);
        nestedListList.add(nestedList2);

        item1.setListObjects(nestedListList);

        ArrayList<ListItem> result = JsonFunctions.getListFromPath("", rootList);

        assertEquals(1,result.size());
        assertEquals("Item1",result.get(0).getName());
        assertEquals("Item2",result.get(0).getListObjects().get(0).get(0).getName());

        result = JsonFunctions.getListFromPath("Item1", rootList);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("Item2", result.get(0).getName());
        assertEquals("test", result.get(0).getValue());
        assertEquals("Item3", result.get(2).getName());
        assertEquals(result.get(1),result.get(3));


        result = JsonFunctions.getListFromPath("Item1///{1}Item3", rootList);

        assertEquals(1, result.size());
        assertEquals("item", result.get(0).getName());
        assertEquals("val", result.get(0).getValue());

    }


    @Test
    public void testGetListFromPathGoBack() {
        ArrayList<ListItem> rootList = new ArrayList<>();
        ListItem item1 = new ListItem();
        item1.setName("Item1");
        rootList.add(item1);
        item1.setIsArrayOfObjects(true);

        ArrayList<ArrayList<ListItem>> nestedListList = new ArrayList<>();

        ArrayList<ListItem> nestedList = new ArrayList<>();
        ListItem item2 = new ListItem();
        item2.setName("Item2");

        ArrayList<ListItem> innerListItem2 = new ArrayList<>();

        ListItem item = new ListItem();
        item.setName("item");
        item.setValue("val");

        innerListItem2.add(item);
        item2.setObjects(innerListItem2);
        nestedList.add(item2);

        nestedListList.add(nestedList);

        item1.setListObjects(nestedListList);

        JsonData data = new JsonData();
        data.setPath("Item1///{0}Item2");

        ArrayList<ListItem> result = JsonFunctions.getListFromPath(data.getPath(), rootList);

        assertEquals(1, result.size());
        assertEquals("item", result.get(0).getName());
        assertEquals("val", result.get(0).getValue());

        data.goBack();

        result = JsonFunctions.getListFromPath(data.getPath(), rootList);

        assertEquals(2, result.size());
        assertEquals("Item2", result.get(0).getName());
        assertTrue(result.get(1).isSpace());

    }


}
