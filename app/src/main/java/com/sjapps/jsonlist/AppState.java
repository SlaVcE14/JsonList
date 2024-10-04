package com.sjapps.jsonlist;

public class AppState {
    boolean hasNewCrash;
    boolean hasCrashLogs;
    boolean MIMEFilterDisabled;
    boolean syntaxHighlighting = true;
    boolean autoCheckForUpdate;
    private int theme;

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

    public boolean isMIMEFilterDisabled() {
        return MIMEFilterDisabled;
    }

    public void setMIMEFilterDisabled(boolean MIMEFilterDisabled) {
        this.MIMEFilterDisabled = MIMEFilterDisabled;
    }

    public boolean isSyntaxHighlighting() {
        return syntaxHighlighting;
    }

    public void setSyntaxHighlighting(boolean syntaxHighlighting) {
        this.syntaxHighlighting = syntaxHighlighting;
    }

    public boolean isAutoCheckForUpdate() {
        return autoCheckForUpdate;
    }

    public void setAutoCheckForUpdate(boolean autoCheckForUpdate) {
        this.autoCheckForUpdate = autoCheckForUpdate;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return "AppState{" +
                "hasNewCrash=" + hasNewCrash +
                ", hasCrashLogs=" + hasCrashLogs +
                ", MIMEFilterDisabled=" + MIMEFilterDisabled +
                ", syntaxHighlighting=" + syntaxHighlighting +
                ", autoCheckForUpdate=" + autoCheckForUpdate +
                ", theme=" + theme +
                '}';
    }
}
