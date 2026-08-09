package com.sjapps.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sj14apps.jsonlist.core.Path;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.databinding.ListPathLayoutBinding;

import java.util.ArrayList;

public class PathListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<String> list;
    Context context;
    MainActivity activity;
    Path path;


    static class ViewHolder extends RecyclerView.ViewHolder{

        ListPathLayoutBinding binding;

        public ViewHolder(ListPathLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public TextView getValTxt(){
            return binding.itemName;
        }
        public View getBtn(){
            return binding.btn;
        }

        public View getView(){
            return itemView;
        }

    }

    public PathListAdapter(Context context, Path path){
        this.context = context;
        this.activity = (MainActivity) context;
        this.path = path;
        this.list = path.splitToArrayString();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListPathLayoutBinding binding = ListPathLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        String item = list.get(pos);

        int position = pos;

        ViewHolder currentHolder = (ViewHolder) holder;
        currentHolder.getValTxt().setText(item);
        currentHolder.getBtn().setOnClickListener(v -> {
            activity.goBack(getLast() - position);
        });

        if (position == getLast())
            currentHolder.binding.arrowImg.setVisibility(View.GONE);
        else currentHolder.binding.arrowImg.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private int getLast(){
        return list.size() -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
