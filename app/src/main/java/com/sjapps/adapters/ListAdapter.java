package com.sjapps.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sjapps.jsonlist.ListItem;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    ArrayList<ListItem> list;
    Context context;
    MainActivity activity;
    String path;
    public int selectedItem = -1;

    public ListAdapter(ArrayList<ListItem> list, Context context,String path){
        this.list = list;
        this.context = context;
        this.activity = (MainActivity) context;
        this.path = path;
    }

    @Override
    public int getCount() {
        if (list == null)
            return 0;

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
            return LayoutInflater.from(context).inflate(R.layout.space_layout, parent, false);
        }
        if (item.isArrayOfObjects() || item.isObject()) {

            View view = LayoutInflater.from(context).inflate(R.layout.list_layout,parent,false);
            TextView titleTxt = view.findViewById(R.id.itemName);
            titleTxt.setText(item.getName());

            if (selectedItem == position){
                view.findViewById(R.id.copyBtn).setVisibility(View.VISIBLE);
            }
            view.findViewById(R.id.btn).setOnClickListener(view1 -> activity.open(item.getName(),path + (path.equals("") ? "": "///" + (item.getId()!=-1?"{" + item.getId() + "}":"")) + item.getName()));
            view.findViewById(R.id.copyBtn).setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Text",item.getName());
                    clipboard.setPrimaryClip(clipData);
                    Toast.makeText(v.getContext(),"Copied to clipboard",Toast.LENGTH_SHORT).show();
                    selectedItem = -1;
                    notifyDataSetChanged();
            });
            view.findViewById(R.id.btn).setOnLongClickListener(v -> {
                selectedItem = position;
                notifyDataSetChanged();
                return true;
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
