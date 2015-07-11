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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final int position = getArguments().getInt("position");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Photo");
        builder.setMessage("Do you realy want to delete this photo?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((SwipeActivity)getActivity()).deleteAfterConfirmation(position);
                Toast.makeText(getActivity(), "Photo has been deleted", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog dialog = builder.create();

        return dialog;
    }


}
