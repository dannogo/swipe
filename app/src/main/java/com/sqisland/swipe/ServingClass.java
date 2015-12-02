package com.sqisland.swipe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by oleh on 9/14/15.
 */
public class ServingClass {

    protected static int squareCounter = 1;
    protected static ArrayList<String> temporaryPhones = new ArrayList<>();
    protected static ArrayList<String> temporaryPhonesIds = new ArrayList<>();
    protected static ArrayList<String> checkedPhones = new ArrayList<>();
    protected static int temporaryPhonesCounter = 0;

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

    protected static void showExtremelyShortToast(Context context, String message){
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 300);
    }

    protected static void shareBtnAction(Context context){

        Intent intent = new Intent();
        intent.setClass(context, ShareActivity.class);
        context.startActivity(intent);

    }

    protected static void temporaryShareForDummyBehavior(Context context){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.google.com");
        sendIntent.setType("text/plain");
//        sendIntent.setType("message/rfc822");
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

                ((PreviewActivity)context).squareBtn.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                ((PreviewActivity)context).squareBtn.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                ((PreviewActivity)context).plusMinus.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                ((PreviewActivity)context).plusMinus.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

                ((PreviewActivity)context).squareCounterView.setVisibility(View.GONE);
                for (int i=0; i<((PreviewActivity)context).recyclerView.getChildCount(); i++){
                    ((PreviewActivity)context).recyclerView.getChildAt(i).findViewById(R.id.small_magnifier).setVisibility(View.GONE);
                }
            }else{
                ((SwipeActivity)context).squareCounterView.setVisibility(View.GONE);

                ((SwipeActivity)context).squareBtn.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                ((SwipeActivity)context).squareBtn.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                ((SwipeActivity)context).plusMinus.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                ((SwipeActivity)context).plusMinus.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

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

                ((PreviewActivity)context).squareBtn.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                ((PreviewActivity)context).squareBtn.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                ((PreviewActivity)context).plusMinus.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                ((PreviewActivity)context).plusMinus.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.small_square_plus);

            }else{
                ((SwipeActivity)context).squareCounterView.setText(String.valueOf(squareCounter));
                ((SwipeActivity)context).squareCounterView.setVisibility(View.VISIBLE);

                ((SwipeActivity)context).squareBtn.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                ((SwipeActivity)context).squareBtn.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                ((SwipeActivity)context).plusMinus.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                ((SwipeActivity)context).plusMinus.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.small_square_plus);

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

    // Gets height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void saveStarsToSharedPreferences(Context context, ArrayList<String> staredPhones){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        int sizeOfArray = staredPhones.size();

        String[] phonesArray = new String[sizeOfArray];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sizeOfArray; i++) {
            phonesArray[i] = staredPhones.get(i);
            sb.append(phonesArray[i]).append(",");
        }
        editor.putString("starsArray", sb.toString());
        Log.w("LOG", sb.toString());
        editor.commit();

    }

    public static void handleExpandCollapseSpeedDial(Context context, FragmentSMS fragmentSMS, boolean expand){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (expand){
            ServingClass.expand(fragmentSMS.speedDial, ((ShareActivity)context).speedDialHeight);
            fragmentSMS.expandSpeedDial.setImageResource(R.drawable.arrow_collapse_50);
            editor.putBoolean("expand", true);
        }else{
            ServingClass.collapse(fragmentSMS.speedDial);
            fragmentSMS.expandSpeedDial.setImageResource(R.drawable.arrow_expand_50);
            editor.putBoolean("expand", false);
        }
        editor.commit();
    }

    public static int handleCountChangeInSpeedDial(Context context, RecyclerView speedDial, boolean onlySize){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        SpeedDialAdapter speedDialAdapter = (SpeedDialAdapter)speedDial.getAdapter();

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) speedDial.getLayoutParams();
        int numberOfColumnsInSpeeddial = context.getResources().getInteger(R.integer.number_of_columns_in_speeddial);
        int heightOfItemInSpeeddial = (int) context.getResources().getDimension(R.dimen.height_of_item_in_speeddial);

        int resultHeight = (speedDialAdapter.phones.size() / numberOfColumnsInSpeeddial * heightOfItemInSpeeddial);
        if ((speedDialAdapter.phones.size() % numberOfColumnsInSpeeddial) != 0) {
            resultHeight += heightOfItemInSpeeddial;
        }
        if (resultHeight > (int) context.getResources().getDimension(R.dimen.max_speeddial_height)){
            resultHeight = (int) context.getResources().getDimension(R.dimen.max_speeddial_height);
            Log.w("HEIGHT", ""+resultHeight);
        }
        ((ShareActivity)context).speedDialHeight = resultHeight;
        params.height = resultHeight;

        speedDial.setLayoutParams(params);

        if (!onlySize) {

            int sizeOfArray = speedDialAdapter.phones.size();

            String[] phonesArray = new String[sizeOfArray];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sizeOfArray; i++) {
                phonesArray[i] = speedDialAdapter.phones.get(i);
                sb.append(phonesArray[i]).append(",");
            }
            editor.putString("phonesArray", sb.toString());

            String[] namesArray = new String[sizeOfArray];
            sb = new StringBuilder();
            for (int i = 0; i < sizeOfArray; i++) {
                namesArray[i] = speedDialAdapter.names.get(i);
                sb.append(namesArray[i]).append(",");
            }
            editor.putString("namesArray", sb.toString());

            String[] photosArray = new String[sizeOfArray];
            sb = new StringBuilder();
            for (int i = 0; i < sizeOfArray; i++) {
                photosArray[i] = speedDialAdapter.photos.get(i);
                sb.append(photosArray[i]).append(",");
            }
            editor.putString("photosArray", sb.toString());

            int[] colorsArray = new int[sizeOfArray];
            sb = new StringBuilder();
            for (int i = 0; i < sizeOfArray; i++) {
                colorsArray[i] = speedDialAdapter.colors.get(i);
                sb.append(colorsArray[i]).append(",");
            }
            editor.putString("colorsArray", sb.toString());

            editor.commit();
        }

        return resultHeight;
    }


    public static void expand(final View v, final int desiredHeight) {

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? desiredHeight
                        : (int)(desiredHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration(300);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    // Show popup window
    protected static void showStatusPopup(final Activity context, Point p) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window, null);

        final PopupWindow popupWindow = new PopupWindow(context);

        View mode1x3 = layout.findViewById(R.id.id1x3);
        View mode2x4 = layout.findViewById(R.id.id2x4);
        View mode3x5 = layout.findViewById(R.id.id3x5);
        View mode4x6 = layout.findViewById(R.id.id4x6);

        if (context instanceof PreviewActivity) {
            mode1x3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    ((PreviewActivity) context).reloadRecyclerView(1, 3);
                }
            });

            mode2x4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    ((PreviewActivity) context).reloadRecyclerView(2, 4);
                }
            });

            mode3x5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    ((PreviewActivity) context).reloadRecyclerView(3, 5);
                }
            });

            mode4x6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    ((PreviewActivity) context).reloadRecyclerView(4, 6);
                }
            });

        }else{
            layout.findViewById(R.id.popupMenuModes).setVisibility(View.GONE);
            layout.findViewById(R.id.popupMenuFirstDivider).setVisibility(View.GONE);
            mode1x3.setVisibility(View.GONE);
            mode2x4.setVisibility(View.GONE);
            mode3x5.setVisibility(View.GONE);
            mode4x6.setVisibility(View.GONE);
        }

        View tellFriend = layout.findViewById(R.id.tellFriend);
        tellFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ServingClass.temporaryShareForDummyBehavior(context);
            }
        });

        View browser = layout.findViewById(R.id.browser);
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
//                context.startActivity(browserIntent);
                Intent intent = new Intent();
                intent.setClass(context, WebActivity.class);
                context.startActivity(intent);
            }
        });

        View help = layout.findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                Intent intent = new Intent();
                intent.setClass(context, YouTubeActivity.class);
                intent.putExtra("video", "t21C09JiRc4");
                context.startActivity(intent);
            }
        });

        View feedback = layout.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

//                int sdkVersion = android.os.Build.VERSION.SDK_INT; //for example 17
                PackageManager manager = context.getPackageManager();
                String version = "unknown";
                try {
                    PackageInfo info = manager.getPackageInfo(
                            context.getPackageName(), 0);
                    version = info.versionName;
                }catch (PackageManager.NameNotFoundException e){
                    Log.e("Exception", e.getMessage());
                }


                String extraText = "[Your feedback here]";
                StringBuilder stringBuilder = new StringBuilder(extraText);

                stringBuilder.append('\n');
                stringBuilder.append('\n');
                stringBuilder.append(" -----------------");
                stringBuilder.append('\n');
                stringBuilder.append("Device name: " + ServingClass.getDeviceName());
                stringBuilder.append('\n');
                stringBuilder.append("OS version: "+android.os.Build.VERSION.RELEASE);
                stringBuilder.append('\n');
                stringBuilder.append("Application version: "+version);



                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","santa_claus@laplandia.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SwipeIV Customer Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        View review = layout.findViewById(R.id.review);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ServingClass.temporaryShareForDummyBehavior(context);
            }
        });

        popupWindow.setContentView(layout);
        popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);

        int OFFSET_X = - 430 ;
        int OFFSET_Y = 50;

        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
    }

    // Returns device name
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        if (manufacturer.equalsIgnoreCase("HTC")) {
            // make sure "HTC" is fully capitalized.
            return "HTC " + model;
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }

// Folders on different devices
//// Lenovo
//        /storage/sdcard1/DCIM/
//        /storage/sdcard1/DCIM/Camera/
//        /storage/sdcard0/VK/
//        /storage/sdcard0/download/
//        /storage/sdcard0/DCIM/
//    DIRECTORY_DCIM﹕ /storage/sdcard0/DCIM
//    DIRECTORY_PICTURES﹕ /storage/sdcard0/Pictures
//    DIRECTORY_DOWNLOADS﹕ /storage/sdcard0/Download
//
//// Nexus
//        /storage/emulated/0/Pictures/Screenshots/
//                /storage/emulated/0/Pictures/OGQ/
//                /storage/emulated/0/DCIM/Camera/
//    DIRECTORY_DCIM﹕ /storage/emulated/0/DCIM
//    DIRECTORY_PICTURES﹕ /storage/emulated/0/Pictures
//    DIRECTORY_DOWNLOADS﹕ /storage/emulated/0/Download

    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int position = parent.getChildAdapterPosition(view);
            if (dataSize > 0 && position == dataSize - 1) {
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }

        }
    }
}

