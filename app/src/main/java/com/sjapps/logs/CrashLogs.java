package com.sjapps.logs;

import java.util.ArrayList;

public class CrashLogs {

    ArrayList<String> logs = new ArrayList<>();

    public void addLog(String s){
        logs.add(s);
    }

    public ArrayList<String> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<String> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "RuntimeExceptions{" +
                "logs=" + logs +
                '}';
    }
}
