package com.sj14apps.jsonlist.core.controllers;

import com.sj14apps.jsonlist.core.JsonNode;
import com.sj14apps.jsonlist.core.ListItem;

public abstract class EditController {


    public boolean isEdited;
    public boolean isEditMode;
    public boolean isApplyingChanges;

    protected abstract void editItem(int position, ListItem item);

    protected abstract void toggleEdit();

    protected void editItem(ListItem item, EditCallBack callBack) {
        if (item.isRootItem()) {
            callBack.rootNotAllowed();
            return;
        }

        if (item.getName() == null) {
            callBack.nameNull();
        } else callBack.showName(item.getName());

        if (item.isArray() || item.isObject()) {
            callBack.valueNull();
        } else callBack.showValue(item.getValue());

        callBack.showEditView(() -> {
            if (item.getName() != null) {
                String name = callBack.newName();
                String oldName = item.getName();

                for (JsonNode i : item.getJsonNode().parent.children) {
                    if (i.key.equals(name) && i != item.getJsonNode()) {
                        callBack.dismissEditView();
                        callBack.sameNameExist();
                        return;
                    }
                }

                if (!oldName.equals(name)) {
                    isEdited = true;
                    if (callBack.hasMultipleItemsInList()) editAllItemsWithSameKey(oldName, name, item);
                }

                item.getJsonNode().setKey(name);
            }

            if (!item.isArray() && !item.isObject()) {
                String value = callBack.newValue();
                if (!item.getValue().equals(value))
                    isEdited = true;
                item.getJsonNode().setValue(value);
            }

            callBack.dismissEditView();
            callBack.update();
        });
    }

    protected abstract void editAllItemsWithSameKey(String oldName, String name, ListItem item);

    public interface ConfirmEditCallBack {
        void onConfirm();
    }

    public interface EditCallBack {
        void rootNotAllowed();

        void nameNull();

        void valueNull();

        void showName(String name);

        void showValue(String value);

        void showEditView(ConfirmEditCallBack callBack);

        void dismissEditView();

        String newName();

        String newValue();

        void sameNameExist();

        boolean hasMultipleItemsInList();
        void update();
    }
}
