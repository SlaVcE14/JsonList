package com.sjapps.jsonlist.controllers;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;

import com.sj14apps.jsonlist.core.JsonFunctions;
import com.sj14apps.jsonlist.core.ListItem;
import com.sj14apps.jsonlist.core.controllers.EditController;
import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sjapps.jsonlist.databinding.ActivityMainBinding;
import com.sjapps.jsonlist.databinding.EditItemBinding;
import com.sjapps.library.customdialog.BasicDialog;
import com.sjapps.library.customdialog.CustomViewDialog;
import com.sjapps.library.customdialog.MessageDialog;

import java.util.Objects;

public class AndroidEditController extends EditController {
    MainActivity activity;
    ActivityMainBinding binding;

    public AndroidEditController(MainActivity activity) {
        this.activity = activity;
        this.binding = activity.binding;
    }

    @Override
    public void editItem(int position, ListItem item) {

        EditItemBinding editItemBinding = EditItemBinding.inflate(LayoutInflater.from(activity));

        EditText nameTxt = editItemBinding.NameTxt;
        EditText valueTxt = editItemBinding.ValueTxt;

        CustomViewDialog dialog = new CustomViewDialog();

        editItem(item, new EditCallBack() {
            @Override
            public void rootNotAllowed() {
                MessageDialog dialog = new MessageDialog();
                dialog.Builder(activity, true)
                        .setTitle(activity.getString(R.string.editing_root_item_not_available))
                        .show();
            }

            @Override
            public void nameNull() {
                editItemBinding.nameLL.setVisibility(GONE);
            }

            @Override
            public void valueNull() {
                editItemBinding.valueLL.setVisibility(GONE);
            }

            @Override
            public void showName(String name) {
                nameTxt.setText(name);
            }

            @Override
            public void showValue(String value) {
                valueTxt.setText(value);
            }

            @Override
            public void showEditView(ConfirmEditCallBack callBack) {
                dialog.Builder(activity, true)
                        .dialogWithTwoButtons()
                        .setTitle(activity.getString(R.string.edit_item))
                        .addCustomView(editItemBinding.getRoot())
                        .onButtonClick(callBack::onConfirm)
                        .show();
                Objects.requireNonNull(dialog.dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }

            @Override
            public void dismissEditView() {
                dialog.dismiss();
            }

            @Override
            public String newName() {
                return nameTxt.getText().toString();
            }

            @Override
            public String newValue() {
                return valueTxt.getText().toString();
            }


            @Override
            public boolean hasMultipleItemsInList() {
                return activity.adapter.itemCountInJSONList > 1;
            }

            @Override
            public void sameNameExist() {
                new MessageDialog().Builder(activity, true)
                        .setTitle(activity.getString(R.string.item_with_name_already_exists))
                        .onDismissListener(d -> dialog.show())
                        .show();
            }

            @Override
            public void update() {
                activity.adapter.notifyItemChanged(position);
                activity.updateFilterList(activity.data.getCurrentList());
            }
        });
    }

    @Override
    public void toggleEdit() {
        if (activity.adapter == null)
            return;

        isEditMode = !activity.adapter.isEditMode();

        if (isEditMode){
            activity.showBackBtn();
            activity.hideToolbar();
            binding.menuBtn.setVisibility(INVISIBLE);
            binding.splitViewBtn.setVisibility(INVISIBLE);
            binding.saveBtn.setVisibility(INVISIBLE);
        }
        else {
            binding.menuBtn.setVisibility(VISIBLE);
            binding.splitViewBtn.setVisibility(VISIBLE);
            activity.hideBackBtnIfNotNeeded();

            if (isEdited){
                activity.loadingStarted(activity.getString(R.string.applying_changes));
                isApplyingChanges = true;
                new Thread(() -> {
                    if (!activity.data.getRawData().equals("-1"))
                        activity.data.setRawData(JsonFunctions.convertToRawString(activity.data.getRootNode()));
                    activity.handler.post(()->{
                        activity.loadingFinished(true);
                        isEdited = false;
                        activity.rawJsonView.isRawJsonLoaded = false;
                        activity.unsavedChanges = true;
                        isApplyingChanges = false;
                        binding.saveBtn.setVisibility(VISIBLE);
                        if (activity.rawJsonView.showJson){
                            activity.rawJsonView.ShowJSON();
                        }
                        activity.showToolbar();
                    });
                }).start();

            }else if (activity.unsavedChanges)
                binding.saveBtn.setVisibility(VISIBLE);
        }

        activity.adapter.setEditMode(isEditMode);
        activity.adapter.notifyItemRangeChanged(0,activity.adapter.getItemCount());
        binding.messageLL.setVisibility(isEditMode ? VISIBLE: GONE);
    }

    @Override
    public void editAllItemsWithSameKey(String oldName, String name, ListItem item) {
        BasicDialog renameAllDialog = new BasicDialog();
        renameAllDialog.Builder(activity, true)
                .setTitle(activity.getString(R.string.rename_all))
                .setMessage(String.format(activity.getString(R.string.rename_all_s_key_with_s), oldName, name))
                .onButtonClick(() -> {
                    for (ListItem listItem : activity.adapter.getList()) {
                        if (listItem.getName() != null && listItem.getName().equals(oldName) && listItem != item) {
                            listItem.getJsonNode().setKey(name);
                            activity.adapter.notifyItemRangeChanged(0, activity.adapter.getItemCount());
                        }
                    }
                    renameAllDialog.dismiss();
                })
                .show();
    }
}
