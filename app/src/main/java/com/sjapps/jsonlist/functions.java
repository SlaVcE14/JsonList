package com.sjapps.jsonlist;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class functions {
    public static String timeFormat(Calendar c){
        if (c == null)
            return "N/A";
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d h:mm:ss a, yyyy");
        return dateFormat.format(c.getTime());
    }

    public static String timeFormatShort(Calendar c){
        if (c == null)
            return "N/A";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(c.getTime());
    }

}
