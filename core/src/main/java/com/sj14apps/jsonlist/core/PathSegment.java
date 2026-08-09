package com.sj14apps.jsonlist.core;

public class PathSegment {
    public final String val;
    public final boolean isId;

    public PathSegment(String val) {
        this.val = val;
        this.isId = false;
    }
    public PathSegment(String val, boolean isId) {
        this.val = val;
        this.isId = isId;
    }
}
