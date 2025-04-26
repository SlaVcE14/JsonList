package com.sjapps.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjapps.jsonlist.functions;
import com.sj14apps.jsonlist.core.JsonData;
import com.sj14apps.jsonlist.core.ListItem;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<ListItem> list;
    Context context;
    MainActivity activity;
    String path;
    public int selectedItem = -1;
    public int highlightedItem = -1;
    boolean isEditMode = false;




    static class ViewHolderShort extends RecyclerView.ViewHolder{

        TextView title;

        public ViewHolderShort(View itemView) {
            super(itemView);
            title =  itemView.findViewById(R.id.itemName);
        }
        public TextView getTitleTxt(){
            return title;
        }

        public View getView(){
            return itemView;
        }

    }

    static class ViewHolderLong extends RecyclerView.ViewHolder{

        TextView title, value;

        public ViewHolderLong(View itemView) {
            super(itemView);
            title =  itemView.findViewById(R.id.itemName);
            value =  itemView.findViewById(R.id.itemValue);
        }
        public TextView getTitleTxt(){
            return title;
        }

        public TextView getValueTxt(){
            return value;
        }

        public View getView(){
            return itemView;
        }

    }

    static class ViewHolderSpace extends RecyclerView.ViewHolder{


        public ViewHolderSpace(View itemView) {
            super(itemView);

        }

        public View getView(){
            return itemView;
        }

    }


    public ListAdapter(ArrayList<ListItem> list, Context context,String path){
        this.list = list;
        this.context = context;
        this.activity = (MainActivity) context;
        this.path = path;
    }

    @Override
    public int getItemViewType(int position) {

        ListItem item = list.get(position);

        return (item.isArray() || item.isObject())?0:item.isSpace()?2:1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout,parent,false);
                return new ViewHolderShort(view);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout2,parent,false);
                return new ViewHolderLong(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.space_layout,parent,false);
                return new ViewHolderSpace(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout,parent,false);
        return new ViewHolderShort(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        ListItem item = list.get(pos);
        if(item.isSpace()) {
            return;
        }

        int position = pos;

        if (item.isArray() || item.isObject()) {

            ViewHolderShort currentHolder = (ViewHolderShort) holder;

            TextView titleTxt = currentHolder.getTitleTxt();
            titleTxt.setText(item.getName());

            View view = currentHolder.getView();

            if (isEditMode){
                view.findViewById(R.id.btn).setOnClickListener(v -> {
                    activity.editItem(pos);
                });
                return;
            }

            if (selectedItem == position){
                view.findViewById(R.id.copyBtn).setVisibility(View.VISIBLE);
            }else view.findViewById(R.id.copyBtn).setVisibility(View.GONE);

            if (highlightedItem == position){
                functions.setAnimation(context,view,R.anim.button_prev,new OvershootInterpolator());
                highlightedItem = -1;
            }

            String newPath = path + (path.equals("") ? "": "///" + (item.getId()!=-1?"{" + item.getId() + "}":"")) + item.getName();

            view.findViewById(R.id.btn).setOnClickListener(view1 -> activity.open(JsonData.getPathFormat(newPath),newPath,item.getPosition()!=-1?item.getPosition():position));
            view.findViewById(R.id.copyBtn).setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Text",item.getName());
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(v.getContext(), R.string.copied_to_clipboard,Toast.LENGTH_SHORT).show();
                selectedItem = -1;
                notifyItemChanged(position);
            });
            view.findViewById(R.id.btn).setOnLongClickListener(v -> {
                notifyItemChanged(selectedItem);
                selectedItem = position;
                notifyItemChanged(position);
                return true;
            });
            return;
        }

        ViewHolderLong currentHolder = (ViewHolderLong) holder;
        View view = currentHolder.getView();
        TextView titleTxt = currentHolder.getTitleTxt();
        TextView valueTxt = currentHolder.getValueTxt();
        if (item.getName() == null)
            titleTxt.setVisibility(View.GONE);
        else {
            titleTxt.setVisibility(View.VISIBLE);
            titleTxt.setText(item.getName());
        }
        view.setClickable(false);

        valueTxt.setText(item.getValue().isEmpty() ? "\"\"" : item.getValue());
        if (isEditMode){
            view.findViewById(R.id.btn).setClickable(true);
            view.findViewById(R.id.btn).setOnClickListener(v -> {
                activity.editItem(pos);
            });
        }

    }

    @Override
    public int getItemCount() {
        if (list == null)
            return 0;

        if (list.size() == 0)
            return 0;

        if (!list.get(getLast()).isSpace())
            return list.size();
        return getLast();
    }

    private int getLast(){
        return (list.size()>0?list.size()-1:0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setHighlightItem(int position){
        highlightedItem = position;
    }

    public ArrayList<ListItem> getList(){
        return list;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

}
