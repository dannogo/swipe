package com.sqisland.swipe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by oleh on 9/14/15.
 */
public class ServingClass {

    protected static void trashBtnAction(Context context, int position){
        RemoveConfirmationDialog dialog = new RemoveConfirmationDialog();
        Bundle data = new Bundle();
        String purpose = "";
        if (context instanceof SwipeActivity) {
            purpose = "SwipeActivity";
            data.putInt("position", position);
        }else if(context instanceof PreviewActivity){
            purpose = "PreviewActivity";
        }else{
            purpose = "PreviewActivity";
            Log.e("ERROR", "There is no information about activity from which trash action was launched");
        }
        data.putString("purpose", purpose);

        dialog.setArguments(data);
        dialog.show(((Activity) context).getFragmentManager(), "Confirmation");
    }

    protected static void shareBtnAction(Context context){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.google.com");
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    protected static void launchCamera(Context context){
        // find out the package of Camera app
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        String pack = intent.resolveActivity(context.getPackageManager()).getPackageName();

        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pack);
        context.startActivity(launchIntent);
    }

    protected static boolean squareBtnAction(Context context, View v, boolean isPlus, SharedPreferences sharedPreferences){
        if (!isPlus){
            ((ImageButton)v).setImageResource(R.drawable.stop_painted);
            ((PreviewActivity)context).squareCounterView.setVisibility(View.GONE);
            PreviewActivity.squareCounter = 0;
            ((ImageButton)((Activity)context).findViewById(R.id.plusMinus)).setImageResource(R.drawable.plus_painted);
            for (int i=0; i<((PreviewActivity)context).recyclerView.getChildCount(); i++){
                ((PreviewActivity)context).recyclerView.getChildAt(i).findViewById(R.id.small_magnifier).setVisibility(View.GONE);
            }

            isPlus = true;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isPlus", isPlus);
            editor.commit();
        }
        return isPlus;
    }

    protected static boolean plusMinusBtnAction(Context context, View v, boolean isPlus, SharedPreferences sharedPreferences){
        if (isPlus){
            ((ImageButton)v).setImageResource(R.drawable.plus_empty);
            ((ImageButton) ((Activity)context).findViewById(R.id.squareBtn)).setImageResource(R.drawable.stop_empty);

            for (int i=0; i<((PreviewActivity)context).recyclerView.getChildCount(); i++){
                ((PreviewActivity)context).recyclerView.getChildAt(i).findViewById(R.id.small_magnifier).setVisibility(View.VISIBLE);
            }

            ((PreviewActivity)context).squareCounterView.setText(String.valueOf(++PreviewActivity.squareCounter));
            ((PreviewActivity)context).squareCounterView.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isPlus", false);
            editor.commit();
        }else{
            ((PreviewActivity)context).squareCounterView.setText(String.valueOf(++PreviewActivity.squareCounter));
        }
        return false;
    }
}