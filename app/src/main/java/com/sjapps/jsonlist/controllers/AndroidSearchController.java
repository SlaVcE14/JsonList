package com.sjapps.jsonlist.controllers;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sj14apps.jsonlist.core.JsonData;
import com.sj14apps.jsonlist.core.SearchItem;
import com.sj14apps.jsonlist.core.controllers.SearchController;
import com.sjapps.adapters.SearchListAdapter;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;

import java.util.ArrayList;

public class AndroidSearchController extends SearchController {

    MainActivity activity;
    SearchListAdapter searchAdapter;

    public AutoTransition autoTransition = new AutoTransition();

    public AndroidSearchController(MainActivity activity){
        this.activity = activity;
        autoTransition.setDuration(150);
    }

    public void appyAdapter(){
        searchAdapter = new SearchListAdapter(activity,new ArrayList<>());
        activity.binding.searchResultList.setAdapter(searchAdapter);
    }

    public void search(JsonData data, String string) {

        if (searchAdapter == null) {
            searchAdapter = new SearchListAdapter(activity, new ArrayList<>());
        }

        super.search(data, string, new SearchEvent() {
            @Override
            public void empty() {
                searchAdapter.setSearchItems(new ArrayList<>());
                searchAdapter.notifyDataSetChanged();
                activity.binding.searchingTxt.setVisibility(GONE);
            }

            @Override
            public void startSearch() {
                activity.binding.searchingTxt.setVisibility(VISIBLE);
                searchAdapter.setSearchItems(new ArrayList<>());
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void result(ArrayList<SearchItem> result) {
                activity.runOnUiThread(() -> {
                    activity.binding.searchingTxt.setVisibility(GONE);
                    searchAdapter.setSearchItems(result);
                    searchAdapter.notifyDataSetChanged();
                });
            }
        });

    }

    @Override
    public void setEvents(JsonData data) {
        ChipGroup searchChipGroup = activity.binding.searchChipGroup;
        searchChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()){
                boolean chip1 = ((Chip) group.getChildAt(0)).isChecked();
                boolean chip2 = ((Chip) group.getChildAt(1)).isChecked();

                if (chip1 && chip2)
                    data.searchMode = 0;
                else if (chip1)
                    data.searchMode = 1;
                else if (chip2)
                    data.searchMode = 2;

            }else data.searchMode = 0;

            if (activity.binding.searchLL.getVisibility() == VISIBLE)
                search(data,activity.binding.searchTxt.getText().toString());
        });

        activity.binding.searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (activity.binding.searchLL.getVisibility() == VISIBLE)
                    search(data,s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        activity.binding.searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activity.binding.searchTxt.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public void showSearchView() {
        TransitionManager.beginDelayedTransition(activity.binding.content, autoTransition);
        activity.binding.mainLL.setVisibility(GONE);
        activity.binding.searchLL.setVisibility(VISIBLE);
        activity.binding.menuBtn.setVisibility(INVISIBLE);
        activity.binding.splitViewBtn.setVisibility(INVISIBLE);
        activity.binding.titleTxt.setText(activity.getString(R.string.search));

        if (activity.binding.backBtn.getVisibility() == GONE)
            activity.binding.backBtn.setVisibility(VISIBLE);

        activity.binding.searchTxt.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(activity.binding.searchTxt, InputMethodManager.SHOW_IMPLICIT);

        appyAdapter();
    }

    @Override
    public void hideSearchView() {
        activity.binding.searchLL.setVisibility(GONE);
        TransitionManager.beginDelayedTransition(activity.binding.content, autoTransition);
        activity.binding.mainLL.setVisibility(VISIBLE);
        activity.binding.menuBtn.setVisibility(VISIBLE);
        activity.binding.splitViewBtn.setVisibility(VISIBLE);
        activity.binding.titleTxt.setText(activity.data.getPath().getFormattedTitle());

        activity.binding.searchTxt.setText("");

        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(activity.binding.searchTxt.getWindowToken(), 0);

        if (activity.data.isEmptyPath()) {
            activity.binding.backBtn.setVisibility(GONE);
        }

    }

}
