package com.sjapps.logs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sj14apps.jsonlist.core.AppState;
import com.sjapps.jsonlist.FileSystem;
import com.sjapps.jsonlist.functions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;


public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    Context context;
    Calendar calendar;


    public CustomExceptionHandler(Context context) {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;

    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {

        AppState state = FileSystem.loadStateData(context);

        if (!state.hasCrashLogs())
            state.setHasCrashLogs(true);

        state.setHasNewCrash(true);
        FileSystem.SaveState(context,new Gson().toJson(state));

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        calendar = Calendar.getInstance();
        String log = "";
        log += "\n\n-- " + functions.timeFormat(calendar) + " -- \n";
        log += stacktrace;

        CrashLogs exceptions = FileSystem.loadLogData(context);

        exceptions.addLog(log);
        FileSystem.SaveLog(context,new Gson().toJson(exceptions));
        defaultUEH.uncaughtException(t, e);
    }
}
