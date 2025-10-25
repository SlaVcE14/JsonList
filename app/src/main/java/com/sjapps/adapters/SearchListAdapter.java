package com.sjapps.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sj14apps.jsonlist.core.JsonData;
import com.sj14apps.jsonlist.core.SearchItem;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class SearchListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<SearchItem> searchItems;
    Context context;
    MainActivity activity;


    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView valTxt;
        TextView pathTxt;
        View btn;

        public ViewHolder(View itemView) {
            super(itemView);
            valTxt = itemView.findViewById(R.id.itemValue);
            pathTxt = itemView.findViewById(R.id.itemPath);
            btn = itemView.findViewById(R.id.btn);
        }
        public TextView getValTxt(){
            return valTxt;
        }
        public TextView getPathTxt(){
            return pathTxt;
        }
        public View getBtn(){
            return btn;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_search_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {

        SearchItem item = searchItems.get(pos);

        ViewHolder currentHolder = (ViewHolder) holder;
        View view = currentHolder.getView();
        TextView valTxt = currentHolder.getValTxt();
        TextView pathTxt = currentHolder.getPathTxt();
        valTxt.setText(item.value);
        String pathStr = "/" + item.path.replace("///","/") + (item.arrayId != -1? "/" + item.arrayId:"");
        pathTxt.setText(pathStr);
        currentHolder.getBtn().setOnClickListener(v -> {
            System.out.println("Search: " + item.id);
            activity.hideSearchView();
            activity.open(JsonData.getPathFormat(item.path),item.path,-1);
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
