package com.sj14apps.jsonlist.core;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;

public class PathTest {

    @Test
    public void testSplitToArrayStringJoiningBug() {
        Path path = new Path();
        path.add("root");
        path.add("0", true);
        path.add("item1");
        path.add("item2");

        ArrayList<String> result = path.splitToArrayString();
        // Expected: ["root", "(0) item1", "item2"]
        assertEquals(3, result.size());
        assertEquals("root", result.get(0));
        assertEquals("(0) item1", result.get(1));
        assertEquals("item2", result.get(2));
    }

    @Test
    public void testGetNodeFromPathSync() {
        // {"a":[{"b":"value}]}
        JsonNode root = new JsonNode().root().object();
        JsonNode a = new JsonNode().array();
        a.setKey("a");
        root.children.add(a);
        
        JsonNode item0 = new JsonNode().object();
        item0.setId(0);
        item0.setParent(a);
        a.children.add(item0);
        
        JsonNode b = new JsonNode();
        b.setKey("b");
        b.setValue("value");
        b.setParent(item0);
        item0.children.add(b);

        JsonData data = new JsonData();
        data.setRootNode(root);
        data.setCurrentNode(b);
        data.getPath().add("a");
        data.getPath().add("0", true);
        data.getPath().add("b");

        // Verify state is at 'b'
        assertEquals(b, data.getCurrentNode());

        // Go back
        data.goBack();
        data.setCurrentNode(JsonFunctions.getNodeFromPath(data.getRootNode(),data.getPath()));
        
        // Path should be "a///0" -> which becomes "a" by fromString/goBack rules usually?
        // Wait, goBack() on "a///0///b" removes "b", then detects "0" is ID and removes it.
        // So path becomes "a".
        assertEquals("a", data.getPath().toString());
        
        // currentNode should now be 'a'
        assertEquals(a, data.getCurrentNode());
    }
}
