package com.sjapps.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;

public class PathListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    String[] list;
    Context context;
    MainActivity activity;
    String path;


    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView valTxt;
        View btn;

        public ViewHolder(View itemView) {
            super(itemView);
            valTxt = itemView.findViewById(R.id.itemName);
            btn = itemView.findViewById(R.id.btn);
        }
        public TextView getValTxt(){
            return valTxt;
        }
        public View getBtn(){
            return btn;
        }

        public View getView(){
            return itemView;
        }

    }

    public PathListAdapter(Context context, String path){
        this.context = context;
        this.activity = (MainActivity) context;
        this.path = path;
        this.list = path.split("///");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_path_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        String item = list[pos];

        int position = pos;

        ViewHolder currentHolder = (ViewHolder) holder;
        View view = currentHolder.getView();
        TextView valTxt = currentHolder.getValTxt();
        valTxt.setText(item);
        currentHolder.getBtn().setOnClickListener(v -> {
            activity.goBack(getLast() - position);
        });

        if (position == getLast())
            view.findViewById(R.id.arrow_img).setVisibility(View.GONE);
        else view.findViewById(R.id.arrow_img).setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    private int getLast(){
        return list.length -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
