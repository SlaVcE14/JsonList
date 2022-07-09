package com.sjapps.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public abstract class ListGenerator implements ListInit{
    ArrayList<AboutListItem> items = new ArrayList<>();
    Context context;

    public ArrayList<AboutListItem> getItems(Context context){
        this.context = context;
        init();
        return items;
    }

    void addItem(String name, String value){
        items.add(new AboutListItem(name,value));
    }
    void addItem(String name, String value, String url){
        items.add(new AboutListItem(name,value,v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        }));
    }
    void addItem(String name, String value, View.OnClickListener listener){
        items.add(new AboutListItem(name,value,listener));
    }
}
