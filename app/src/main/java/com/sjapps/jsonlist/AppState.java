package com.sjapps.jsonlist;

public class AppState {
    boolean hasNewCrash;
    boolean hasCrashLogs;

    public boolean hasNewCrash() {
        return hasNewCrash;
    }

    public void setHasNewCrash(boolean hasNewCrash) {
        this.hasNewCrash = hasNewCrash;
    }

    public boolean hasCrashLogs() {
        return hasCrashLogs;
    }

    public void setHasCrashLogs(boolean hasCrashLogs) {
        this.hasCrashLogs = hasCrashLogs;
    }

    @Override
    public String toString() {
        return "AppState{" +
                "hasNewCrash=" + hasNewCrash +
                ", hasCrashLogs=" + hasCrashLogs +
                '}';
    }
}
