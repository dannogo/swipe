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

    protected static int squareCounter = 1;

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
            squareCounter = 1;
            ((ImageButton)((Activity)context).findViewById(R.id.plusMinus)).setImageResource(R.drawable.plus_empty);
            if (context instanceof PreviewActivity){

                ((PreviewActivity)context).squareBtn.getLayoutParams().height = 120;
                ((PreviewActivity)context).squareBtn.getLayoutParams().width = 120;
                ((PreviewActivity)context).squareBtn.requestLayout();
                ((PreviewActivity)context).plusMinus.getLayoutParams().height = 150;
                ((PreviewActivity)context).plusMinus.getLayoutParams().width = 150;
                ((PreviewActivity)context).plusMinus.requestLayout();

                ((PreviewActivity)context).squareCounterView.setVisibility(View.GONE);
                for (int i=0; i<((PreviewActivity)context).recyclerView.getChildCount(); i++){
                    ((PreviewActivity)context).recyclerView.getChildAt(i).findViewById(R.id.small_magnifier).setVisibility(View.GONE);
                }
            }else{
                ((SwipeActivity)context).squareCounterView.setVisibility(View.GONE);

                ((SwipeActivity)context).squareBtn.getLayoutParams().height = 120;
                ((SwipeActivity)context).squareBtn.getLayoutParams().width = 120;
                ((SwipeActivity)context).squareBtn.requestLayout();
                ((SwipeActivity)context).plusMinus.getLayoutParams().height = 150;
                ((SwipeActivity)context).plusMinus.getLayoutParams().width = 150;
                ((SwipeActivity)context).plusMinus.requestLayout();

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
            ((ImageButton)v).setImageResource(R.drawable.plus_painted);
            ((ImageButton) ((Activity)context).findViewById(R.id.squareBtn)).setImageResource(R.drawable.stop_empty);

            if (context instanceof PreviewActivity) {
                for (int i = 0; i < ((PreviewActivity) context).recyclerView.getChildCount(); i++) {
                    ((PreviewActivity) context).recyclerView.getChildAt(i).findViewById(R.id.small_magnifier).setVisibility(View.VISIBLE);
                }
                ((PreviewActivity)context).squareCounterView.setText(String.valueOf(squareCounter));
                ((PreviewActivity)context).squareCounterView.setVisibility(View.VISIBLE);

                ((PreviewActivity)context).squareBtn.getLayoutParams().height = 150;
                ((PreviewActivity)context).squareBtn.getLayoutParams().width = 150;
                ((PreviewActivity)context).squareBtn.requestLayout();
                ((PreviewActivity)context).plusMinus.getLayoutParams().height = 120;
                ((PreviewActivity)context).plusMinus.getLayoutParams().width = 120;
                ((PreviewActivity)context).plusMinus.requestLayout();

            }else{
                ((SwipeActivity)context).squareCounterView.setText(String.valueOf(squareCounter));
                ((SwipeActivity)context).squareCounterView.setVisibility(View.VISIBLE);

                ((SwipeActivity)context).squareBtn.getLayoutParams().height = 150;
                ((SwipeActivity)context).squareBtn.getLayoutParams().width = 150;
                ((SwipeActivity)context).squareBtn.requestLayout();
                ((SwipeActivity)context).plusMinus.getLayoutParams().height = 120;
                ((SwipeActivity)context).plusMinus.getLayoutParams().width = 120;
                ((SwipeActivity)context).plusMinus.requestLayout();

            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isPlus", false);
            editor.commit();
        }else{
            if (context instanceof PreviewActivity) {
                ((PreviewActivity) context).squareCounterView.setText(String.valueOf(++squareCounter));
            }else{
                ((SwipeActivity) context).squareCounterView.setText(String.valueOf(++squareCounter));
            }
        }
        return false;
    }
}