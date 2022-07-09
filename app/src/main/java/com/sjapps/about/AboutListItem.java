package com.sjapps.about;

import android.view.View;

public class AboutListItem {
    private String itemName;
    private String itemValue;
    private View.OnClickListener onClickListener;

    public AboutListItem(String itemName, String itemValue) {
        this.itemName = itemName;
        this.itemValue = itemValue;
    }
    public static AboutListItem item(String itemName, String itemValue){
        return new AboutListItem(itemName,itemValue);
    }
    public AboutListItem(String itemName, String itemValue, View.OnClickListener listener) {
        this.itemName = itemName;
        this.itemValue = itemValue;
        this.onClickListener = listener;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
