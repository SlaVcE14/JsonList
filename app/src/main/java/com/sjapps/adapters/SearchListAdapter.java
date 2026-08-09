package com.sjapps.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sj14apps.jsonlist.core.JsonData;
import com.sj14apps.jsonlist.core.Path;
import com.sj14apps.jsonlist.core.SearchItem;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.databinding.ListSearchLayoutBinding;

import java.util.ArrayList;

public class SearchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<SearchItem> searchItems;
    Context context;
    MainActivity activity;


    static class ViewHolder extends RecyclerView.ViewHolder{

        ListSearchLayoutBinding binding;

        public ViewHolder(ListSearchLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
        public TextView getValTxt(){
            return binding.itemValue;
        }
        public TextView getPathTxt(){
            return binding.itemPath;
        }
        public View getBtn(){
            return binding.btn;
        }

        public View getView(){
            return itemView;
        }

    }

    public SearchListAdapter(Context context, ArrayList<SearchItem> searchItems){
        this.context = context;
        this.activity = (MainActivity) context;
        this.searchItems = searchItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListSearchLayoutBinding binding = ListSearchLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        SearchItem item = searchItems.get(pos);

        ViewHolder currentHolder = (ViewHolder) holder;
        View view = currentHolder.getView();
        TextView valTxt = currentHolder.getValTxt();
        TextView pathTxt = currentHolder.getPathTxt();
        valTxt.setText(item.value);
        String pathStr = item.getDisplayPath();
        pathTxt.setText(pathStr);
        currentHolder.getBtn().setOnClickListener(v -> {
            activity.searchController.hideSearchView();
            Path newPath = item.path.copy();
            if (!newPath.isEmpty()) newPath.goBack();
            activity.open(newPath.getFormattedTitle(),item.node.parent,newPath,-1);
            activity.highlightItem(item.id);
        });

    }

    public void setSearchItems(ArrayList<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }

    @Override
    public int getItemCount() {
        return searchItems.size();
    }

    private int getLast(){
        return searchItems.size() -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
