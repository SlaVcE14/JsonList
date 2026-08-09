package com.sj14apps.jsonlist.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

public class GetListFromPathTest {

    @Test
    public void testGetNodeFromPathWithNestedStructureObject() {
        JsonNode rootNode = new JsonNode().root().object();

        JsonNode item1 = new JsonNode().object();
        item1.setKey("Item1");
        item1.setParent(rootNode);

        JsonNode item2 = new JsonNode().object();
        item2.setKey("Item2");
        item2.setParent(item1);

        JsonNode item3 = new JsonNode();
        item3.setKey("Item3");
        item3.setValue("test");
        item3.setParent(item2);

        JsonNode item4 = new JsonNode();
        item4.setKey("Item4");
        item4.setValue("123");
        item4.setParent(item2);

        item2.children.add(item3);
        item2.children.add(item4);
        item1.children.add(item2);
        rootNode.children.add(item1);

        Path path = new Path();
        path.add("Item1");

        JsonNode resultNode = JsonFunctions.getNodeFromPath(rootNode, path);
        assertNotNull(resultNode);
        assertEquals("Item1", resultNode.key);

        ArrayList<ListItem> resultList = JsonFunctions.getListFromNode(resultNode);
        assertEquals(1, resultList.size());
        assertEquals("Item2", resultList.get(0).getName());

        path.add("Item2");
        resultNode = JsonFunctions.getNodeFromPath(rootNode, path);
        assertNotNull(resultNode);
        assertEquals("Item2", resultNode.key);
        
        resultList = JsonFunctions.getListFromNode(resultNode);
        assertEquals(2, resultList.size());
        assertEquals("Item3", resultList.get(0).getName());
        assertEquals("test", resultList.get(0).getValue());
        assertEquals("Item4", resultList.get(1).getName());
        assertEquals("123", resultList.get(1).getValue());
    }

    @Test
    public void testGetNodeFromPathWithNestedStructureArrayOfObjects() {
        JsonNode rootNode = new JsonNode().root().array();

        JsonNode item1 = new JsonNode().array();
        item1.setKey("Item1");
        item1.setParent(rootNode);

        JsonNode nested1 = new JsonNode().object();
        nested1.setId(0);
        nested1.setParent(item1);
        
        JsonNode item2 = new JsonNode();
        item2.setKey("Item2");
        item2.setValue("test");
        item2.setParent(nested1);
        nested1.children.add(item2);

        JsonNode nested2 = new JsonNode().object();
        nested2.setId(1);
        nested2.setParent(item1);

        JsonNode item3 = new JsonNode().object();
        item3.setKey("Item3");
        item3.setParent(nested2);

        JsonNode item = new JsonNode();
        item.setKey("item");
        item.setValue("val");
        item.setParent(item3);
        item3.children.add(item);
        
        nested2.children.add(item3);

        item1.children.add(nested1);
        item1.children.add(nested2);
        rootNode.children.add(item1);

        Path path = new Path();
        JsonNode resultNode = JsonFunctions.getNodeFromPath(rootNode, path);
        assertEquals(rootNode, resultNode);
        
        ArrayList<ListItem> resultList = JsonFunctions.getListFromNode(resultNode);
        assertEquals(1, resultList.size()); // item1 (array, no space added for non-object items)
        assertEquals("Item1", resultList.get(0).getName());

        path.add("Item1");
        resultNode = JsonFunctions.getNodeFromPath(rootNode, path);
        assertNotNull(resultNode);
        resultList = JsonFunctions.getListFromNode(resultNode);
        assertEquals(4, resultList.size()); // Item2, space, Item3, space

        path.add("1", true); // id 1
        path.add("Item3");
        resultNode = JsonFunctions.getNodeFromPath(rootNode, path);
        assertNotNull(resultNode);
        assertEquals("Item3", resultNode.key);

        resultList = JsonFunctions.getListFromNode(resultNode);
        assertEquals(1, resultList.size());
        assertEquals("item", resultList.get(0).getName());
        assertEquals("val", resultList.get(0).getValue());
    }

    @Test
    public void testGetNodeFromPathGoBack() {
        JsonData data = new JsonData();
        JsonNode rootNode = new JsonNode().root().array();

        JsonNode item1 = new JsonNode().array();
        item1.setKey("Item1");
        item1.setParent(rootNode);

        JsonNode nested1 = new JsonNode().object();
        nested1.setId(0);
        nested1.setParent(item1);
        
        JsonNode item2 = new JsonNode().object();
        item2.setKey("Item2");
        item2.setParent(nested1);

        JsonNode item = new JsonNode();
        item.setKey("item");
        item.setValue("val");
        item.setParent(item2);
        item2.children.add(item);
        
        nested1.children.add(item2);
        item1.children.add(nested1);
        rootNode.children.add(item1);

        data.setRootNode(rootNode);
        
        Path path = new Path();
        path.add("Item1");
        path.add("0", true);
        path.add("Item2");
        data.setPath(path);

        JsonNode resultNode = JsonFunctions.getNodeFromPath(data.getRootNode(), data.getPath());
        ArrayList<ListItem> resultList = JsonFunctions.getListFromNode(resultNode);

        assertEquals(1, resultList.size());
        assertEquals("item", resultList.get(0).getName());
        assertEquals("val", resultList.get(0).getValue());

        data.goBack();

        resultNode = JsonFunctions.getNodeFromPath(data.getRootNode(), data.getPath());
        resultList = JsonFunctions.getListFromNode(resultNode);

        assertEquals(2, resultList.size());
        assertEquals("Item2", resultList.get(0).getName());
        assertTrue(resultList.get(1).isSpace());
    }
}
