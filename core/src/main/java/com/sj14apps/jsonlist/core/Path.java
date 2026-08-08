package com.sj14apps.jsonlist.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Path {
    Deque<PathSegment> pathSegments = new ArrayDeque<>();

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
                builder.append("///");
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
        String[] segments = path.split("///");
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
}
