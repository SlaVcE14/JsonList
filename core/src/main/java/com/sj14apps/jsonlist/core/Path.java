package com.sj14apps.jsonlist.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Path {
    public Deque<PathSegment> pathSegments = new ArrayDeque<>();
    static int maxPathNameLength = 3;

    public Path() {
    }

    public Path(Deque<PathSegment> pathSegments) {
        this.pathSegments = pathSegments;
    }

    public void add(String val) {
        add(val, false);
    }

    public void add(String val, boolean isId) {
        add(new PathSegment(val, isId));
    }

    public void add(PathSegment segment) {
        pathSegments.addLast(segment);
    }

    public PathSegment goBack() {
        PathSegment segment = pathSegments.removeLast();
        if (!pathSegments.isEmpty() && pathSegments.peekLast().isId)
            pathSegments.removeLast();
        return segment;
    }

    public static final String DELIMITER = "///";

    public ArrayList<String> splitToArrayString() {
        ArrayList<String> list = new ArrayList<>();
        boolean hasId = false;
        for (PathSegment segment : pathSegments) {
            String val;
            if (segment.isId) {
                val = "(" + segment.val + ")";
                hasId = true;
                list.add(val);
                continue;
            }
            if (hasId) {
                list.set(list.size() - 1, list.get(list.size() - 1) + " " + segment.val);
                hasId = false;
                continue;
            }
            val = segment.val;
            list.add(val);

        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (PathSegment segment : pathSegments) {
            builder.append(segment.val);
            if (i < pathSegments.size() - 1)
                builder.append(DELIMITER);
            i++;
        }
        return builder.toString();
    }

    public boolean isEmpty() {
        return pathSegments.isEmpty();
    }

    public Path copy() {
        return new Path(new ArrayDeque<>(pathSegments));
    }

    public Path fromString(String path) {
        if (path == null || path.isEmpty())
            return this;
        String[] segments = path.split(DELIMITER);
        for (String s : segments) {
            if (s.matches("\\d+"))
                add(s, true);
            else
                add(s);
        }
        if (!pathSegments.isEmpty() && pathSegments.peekLast().isId)
            pathSegments.removeLast();
        return this;
    }

    public String getDisplayPath() {
        StringBuilder builder = new StringBuilder();
        builder.append("/");
        for (PathSegment segment : pathSegments) {
            builder.append(segment.val).append("/");
        }
        if (builder.length() > 1) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    public String getFormattedTitle() {
        if (pathSegments.isEmpty()) return "";
        
        ArrayList<String> displayNames = splitToArrayString();
        StringBuilder builder = new StringBuilder();

        int startIndex = displayNames.size() > maxPathNameLength ? displayNames.size() - maxPathNameLength : 1;
        builder.append(displayNames.size() > maxPathNameLength ? "..." : displayNames.get(0));

        for (int i = startIndex; i < displayNames.size(); i++) {
            builder.append("/").append(getName(displayNames.get(i)));
        }

        return builder.toString();
    }

    private String getName(String str) {
        if (str.startsWith("(") && str.contains(")") && str.substring(1, str.indexOf(")")).matches("^[0-9]+"))
            return str.substring(str.indexOf(")") + 1).trim();
        return str;
    }
}
