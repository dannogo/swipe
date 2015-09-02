package com.sqisland.swipe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by oleh on 9/2/15.
 */
public class FavoritesConfirmation extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int position = getArguments().getInt("position");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add to favorites");
        builder.setMessage("Do you realy want to add this media file to favorites?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SivAdapter.favoritesUri.add(((SwipeActivity) getActivity()).images.get(position));
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Set<String> favUriSet = new HashSet<>();
                for (int i=0; i<SivAdapter.favoritesUri.size(); i++){
                    favUriSet.add(SivAdapter.favoritesUri.get(i).toString());
                }
                editor.putStringSet("favoritesUri", favUriSet);
                editor.putBoolean("swipe_activity_favorites_changes", true);
                editor.commit();

                ImageButton starInToolbar = (ImageButton) getActivity().findViewById(R.id.starBtn);
                starInToolbar.setVisibility(View.GONE);
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.view_pager);
                ImageView star = (ImageView) viewPager.findViewWithTag("star_" + position);

                star.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), R.string.addingToFavorites, Toast.LENGTH_SHORT).show();
            }
        });
        Dialog dialog = builder.create();

        return dialog;
    }
}
