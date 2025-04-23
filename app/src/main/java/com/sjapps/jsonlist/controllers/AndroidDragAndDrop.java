package com.sjapps.jsonlist.controllers;

import android.content.ClipData;
import android.net.Uri;
import android.os.Build;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sjapps.jsonlist.MainActivity;
import com.sjapps.jsonlist.R;
import com.sjapps.jsonlist.functions;

public class AndroidDragAndDrop {

    MainActivity activity;
    TextView dropTargetTxt;
    View dropTargetBackground;
    RelativeLayout dropTarget;
    DragAndDropCallback callback;

    public AndroidDragAndDrop(MainActivity activity, DragAndDropCallback callback){
        this.activity = activity;

        dropTarget = activity.findViewById(R.id.dropTarget);
        dropTargetTxt = activity.findViewById(R.id.dropTargetText);
        dropTargetBackground = activity.findViewById(R.id.dropTargetBackground);
        this.callback = callback;
        setEvent();
    }

    void setEvent() {
        dropTarget.setOnDragListener((v, event) -> {

            String MIMEType = activity.state.isMIMEFilterDisabled()?"*/*": Build.VERSION.SDK_INT > Build.VERSION_CODES.P?"application/json":"application/*";

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: return onDragStarted();
                case DragEvent.ACTION_DRAG_ENTERED: return onDragEntered(event, MIMEType);
                case DragEvent.ACTION_DRAG_EXITED: return onDragExited();
                case DragEvent.ACTION_DRAG_ENDED: return onDragEnded();
                case DragEvent.ACTION_DROP: return onDropped(event, MIMEType, callback);
            }
            return false;
        });
    }

    private boolean onDragStarted(){
        dropTarget.setAlpha(1);
        return true;
    }

    private boolean onDragEntered(DragEvent event, String MIMEType){
        dropTarget.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        if(event.getClipDescription().getMimeTypeCount() > 1){
            dropTargetTxt.setText(R.string.only_one_file_is_allowed);
            dropTargetBackground.getBackground().mutate().setTint(functions.setColor(activity, R.attr.colorError));
            dropTargetBackground.setAlpha(.8f);
            return false;
        }
        if (!event.getClipDescription().hasMimeType(MIMEType)) {
            dropTargetTxt.setText(R.string.this_is_not_json_file);
            dropTargetBackground.getBackground().mutate().setTint(functions.setColor(activity, R.attr.colorError));
            dropTargetBackground.setAlpha(.8f);
            return false;
        }

        dropTargetBackground.getBackground().mutate().setTint(functions.setColor(activity, R.attr.colorPrimary));
        dropTargetBackground.setAlpha(.8f);
        return true;
    }

    private boolean onDragExited(){
        dropTargetTxt.setText(R.string.drop_json_file_here);
        dropTargetBackground.getBackground().mutate().setTint(functions.setColor(activity, R.attr.colorOnBackground));
        dropTargetBackground.setAlpha(.5f);
        return true;
    }

    private boolean onDragEnded(){
        dropTargetTxt.setText(R.string.drop_json_file_here);
        dropTargetBackground.getBackground().mutate().setTint(functions.setColor(activity, R.attr.colorOnBackground));
        dropTarget.setAlpha(0);
        return true;
    }

    private boolean onDropped(DragEvent event, String MIMEType, DragAndDropCallback callback){
        if (event.getClipData().getItemCount() > 1){
            return false;
        }
        if (!event.getClipDescription().hasMimeType(MIMEType))
            return false;

        if (callback.checkIfFileIsLoading())
            return false;


        ClipData.Item item = event.getClipData().getItemAt(0);

        DragAndDropPermissions dropPermissions = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            dropPermissions = activity.requestDragAndDropPermissions(event);

        callback.onDrop(item.getUri());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && dropPermissions != null)
            dropPermissions.release();
        return true;
    }

    public interface DragAndDropCallback {
        boolean checkIfFileIsLoading();
        void onDrop(Uri uri);
    }


}
