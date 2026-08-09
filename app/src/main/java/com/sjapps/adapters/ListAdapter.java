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

import com.sj14apps.jsonlist.core.Path;
import com.sjapps.jsonlist.databinding.ListLayout2Binding;
import com.sjapps.jsonlist.databinding.ListLayoutBinding;
import com.sjapps.jsonlist.databinding.SpaceLayoutBinding;
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
    Path path;
    public int selectedItem = -1;
    public int highlightedItem = -1;
    boolean isEditMode = false;
    public int itemCountInJSONList = 0;


    static class ViewHolderShort extends RecyclerView.ViewHolder{
        ListLayoutBinding binding;

        public ViewHolderShort(ListLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public TextView getTitleTxt(){
            return binding.itemName;
        }

        public ListLayoutBinding getBinding(){
            return binding;
        }

        public View getView(){
            return itemView;
        }

    }

    static class ViewHolderLong extends RecyclerView.ViewHolder{

        ListLayout2Binding binding;

        public ViewHolderLong(ListLayout2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public TextView getTitleTxt(){
            return binding.itemName;
        }
        public TextView getValueTxt(){
            return binding.itemValue;
        }

        public View getView(){
            return itemView;
        }

        public ListLayout2Binding getBinding() {
            return binding;
        }

    }

    static class ViewHolderSpace extends RecyclerView.ViewHolder{


        public ViewHolderSpace(SpaceLayoutBinding binding) {
            super(binding.getRoot());

        }

        public View getView(){
            return itemView;
        }

    }


    public ListAdapter(ArrayList<ListItem> list, Context context,Path path){
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
            case 1:
                ListLayout2Binding longItemBinding = ListLayout2Binding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new ViewHolderLong(longItemBinding);
            case 2:
                SpaceLayoutBinding spaceBinding = SpaceLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new ViewHolderSpace(spaceBinding);
            default:
                ListLayoutBinding shortItemBinding = ListLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
                return new ViewHolderShort(shortItemBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        ListItem item = list.get(pos);
        if(item.isSpace()) {
            itemCountInJSONList++;
            return;
        }

        int position = pos;

        if (item.isArray() || item.isObject()) {

            ViewHolderShort currentHolder = (ViewHolderShort) holder;

            TextView titleTxt = currentHolder.getTitleTxt();
            titleTxt.setText(item.getName());

            View view = currentHolder.getView();

            if (isEditMode){
                currentHolder.binding.btn.setOnClickListener(v -> {
                    activity.editItem(pos);
                });
                currentHolder.binding.btn.setOnLongClickListener(null);
                currentHolder.binding.copyBtn.setVisibility(View.GONE);
                return;
            }

            if (selectedItem == position){
                currentHolder.binding.copyBtn.setVisibility(View.VISIBLE);
            }else currentHolder.binding.copyBtn.setVisibility(View.GONE);

            if (highlightedItem == position){
                functions.setAnimation(context,view,R.anim.button_prev,new OvershootInterpolator());
                highlightedItem = -1;
            }

            Path newPath = path.copy();
            if (!item.isSpace() && item.getJsonNode().parent.id != null)
                newPath.add(item.getJsonNode().parent.id+"",true);
            newPath.add(item.getName());

            currentHolder.binding.btn.setOnClickListener(view1 -> activity.open(newPath.getFormattedTitle(),item.getJsonNode(),newPath,item.getPosition()!=-1?item.getPosition():position));
            currentHolder.binding.copyBtn.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Text",item.getName());
                clipboard.setPrimaryClip(clipData);
                Toast.makeText(v.getContext(), R.string.copied_to_clipboard,Toast.LENGTH_SHORT).show();
                selectedItem = -1;
                notifyItemChanged(position);
            });
            currentHolder.binding.btn.setOnLongClickListener(v -> {
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
            setTextClickable(currentHolder.getTitleTxt(),false);
            setTextClickable(currentHolder.getValueTxt(),false);
            currentHolder.binding.btn.setClickable(true);
            currentHolder.binding.btn.setBackgroundResource(R.drawable.ripple_list2);

            currentHolder.binding.btn.setOnClickListener(v -> {
                activity.editItem(pos);
            });
        } else {
            currentHolder.binding.btn.setOnClickListener(null);
            currentHolder.binding.btn.setBackgroundResource(R.drawable.background);
            setTextClickable(currentHolder.getTitleTxt(),true);
            setTextClickable(currentHolder.getValueTxt(),true);
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
        selectedItem = -1;
        this.isEditMode = isEditMode;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    private void setTextClickable(TextView textView, boolean clickable) {
        textView.setLongClickable(clickable);
        textView.setTextIsSelectable(clickable);
        textView.setFocusable(clickable);
        textView.setFocusableInTouchMode(clickable);
    }

}
