package com.sqisland.swipe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by oleh on 7/11/15.
 */
public class RemoveConfirmationDialog extends DialogFragment {

    String purpose;
    int position;
    String notification;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = new String();
        String message = new String();

        purpose = getArguments().getString("purpose");
        if (purpose.equals("SwipeActivity")) {
            position = getArguments().getInt("position");
            title = "Delete Photo";
            message = "Do you realy want to delete this photo?";
            notification = "Photo has been deleted";
        }else if(purpose.equals("PreviewActivity")){
            title = "Delete Photos";
            message = "Do you realy want to delete checked photos?";
            notification = "Photos have been deleted";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (purpose.equals("SwipeActivity")) {
                    ((SwipeActivity) getActivity()).deleteAfterConfirmation(position);
                }else if(purpose.equals("PreviewActivity")) {
                    ((PreviewActivity)getActivity()).deleteChecked();
                    ((PreviewActivity)getActivity()).dissmisDeleteMode();
                }
                    Toast.makeText(getActivity(), notification, Toast.LENGTH_SHORT).show();
            }
        });
        Dialog dialog = builder.create();

        return dialog;
    }


}
