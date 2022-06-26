package com.sjapps.about;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sjapps.jsonlist.R;
import java.util.ArrayList;

public class AboutListAdapter extends RecyclerView.Adapter<AboutListAdapter.ViewHolder> {

    ArrayList<AboutListItem> Items;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView NameTxt, ValueTxt;

        public ViewHolder(View itemView) {
            super(itemView);
            NameTxt = itemView.findViewById(R.id.NameTxt);
            ValueTxt = itemView.findViewById(R.id.ValueTxt);
        }

        public TextView getNameTxt(){
            return NameTxt;
        }
        public TextView getValueTxt(){
            return ValueTxt;
        }
    }

    public AboutListAdapter(ArrayList<AboutListItem> items) {
        Items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.about_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getNameTxt().setText(Items.get(position).getItemName());
        holder.getValueTxt().setText(Items.get(position).getItemValue());

    }

    @Override
    public int getItemCount() {
        return Items.size();
    }


}
