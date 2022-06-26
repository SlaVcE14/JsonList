package com.sjapps.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sjapps.jsonlist.ListItem;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    ArrayList<ListItem> list;
    Context context;
    MainActivity activity;
    String path;

    public ListAdapter(ArrayList<ListItem> list, Context context,String path){
        this.list = list;
        this.context = context;
        this.activity = (MainActivity) context;
        this.path = path;
    }

    @Override
    public int getCount() {

        if (list.size() == 0)
            return 0;

        if (!list.get(getLast()).isSpace())
            return list.size();
        return getLast();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private int getLast(){
        return (list.size()>0?list.size()-1:0);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListItem item = list.get(position);
        if(item.isSpace()) {
            View view = LayoutInflater.from(context).inflate(R.layout.space_layout, parent, false);
            return view;
        }
        if (item.isArrayOfObjects() || item.isObject()) {

            View view = LayoutInflater.from(context).inflate(R.layout.list_layout,parent,false);
            TextView titleTxt = view.findViewById(R.id.itemName);
            titleTxt.setText(item.getName());

            view.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.open(item.getName(),path + (path.equals("") ? "":"///") + item.getName());
                }
            });
            return view;

        }
        View view = LayoutInflater.from(context).inflate(R.layout.list_layout2,parent,false);
        TextView titleTxt = view.findViewById(R.id.itemName);
        TextView valueTxt = view.findViewById(R.id.itemValue);
        view.setClickable(false);
        titleTxt.setText(item.getName());
        valueTxt.setText(item.getValue());

        return view;
    }
}
