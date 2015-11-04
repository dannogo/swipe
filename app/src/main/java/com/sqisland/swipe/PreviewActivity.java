package com.sqisland.swipe;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



public class PreviewActivity extends ActionBarActivity {

    protected RecyclerView recyclerView;
    protected SivAdapter adapter;
    private ArrayList<String> images;
    protected static boolean deletedItemsInSwipeActivity = false;
    protected String lastMediaUril;
    private SharedPreferences sharedPreferences;
    private LinearLayout toolbar;
    private View statusBar;
    private boolean isDeleteMode;
    private ImageButton trashBtn;
    private ImageButton cancelBtn;
    protected TextView info;
    protected boolean isPlus;
    private ImageButton squareBtn;
    private ImageButton plusMinus;
    protected static String filter;
    private int columnsInPortrait;
    private int columnsInLandscape;
    protected static ImageButton starBtn;
    private PopupWindow popupWindow;
    private LinearLayout filterTab;
    protected TextView squareCounterView;
    protected static int squareCounter = 0;


    // Completely deletes photo from Gallery folder
    public static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }



    public void deleteChecked(String purpose){
        ArrayList<String>  urisForDeleting = new ArrayList<>();
        ArrayList<File> filesForDeleting = new ArrayList<>();
        Set<String> set = sharedPreferences.getStringSet("checkedItems", new HashSet<String>());
        Iterator iter = set.iterator();
        ArrayList<String> tempList = new ArrayList<String>(images);
        while(iter.hasNext()){
            String str = tempList.get(Integer.parseInt((String) iter.next()));
            if (SivAdapter.favoritesUri.contains(str)){
                if (filter.equals("Favorites") && purpose.equals("PreviewActivity")){
                    SivAdapter.favoritesUri.remove(str);

                    filesForDeleting.add(new File(str));
                    urisForDeleting.add(str);
                }else{
                    Toast.makeText(this, "Can`t delete favorite media not from favorites.", Toast.LENGTH_SHORT).show();
                }
            }else {
                filesForDeleting.add(new File(str));
                urisForDeleting.add(str);
            }

        }

        images = getCameraImages(urisForDeleting);
        if (images.size()>0) {
            lastMediaUril = images.get(0);
        }

        DeleteTask deleteTask = new DeleteTask();
        deleteTask.execute(filesForDeleting);

        SivAdapter.checkedItems = new ArrayList<Integer>();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        set = new HashSet<>();
        Set<String> favUriSet = new HashSet<>();
        for (int i=0; i<SivAdapter.favoritesUri.size(); i++){
            favUriSet.add(SivAdapter.favoritesUri.get(i).toString());
        }

        editor.putStringSet("checkedItems", set);
        editor.putStringSet("favoritesUri", favUriSet);
        editor.commit();

        reloadRecyclerView(columnsInPortrait, columnsInLandscape);
    }

    // gets list of photo's uris. Second argument helps to avoid rendering just deleted photo
    public ArrayList<String> getCameraImages(ArrayList<String> deleted) {
        // Get relevant columns for use later.
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };

        String selection = "";

        if (filter.equals("Photo")){
            info.setText("Photo");
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        }else if (filter.equals("Video")){
            info.setText("Video");
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        }else if (filter.equals("All")){
            info.setText("All");
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        }else if (filter.equals("Favorites")){
            info.setText("Favorites");
            return SivAdapter.favoritesUri;
        }

        Uri queryUri = MediaStore.Files.getContentUri("external");

        CursorLoader cursorLoader = new CursorLoader(
                this,
                queryUri,
                projection,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );

        Cursor cursor = cursorLoader.loadInBackground();

        ArrayList<String> result = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                final String data = cursor.getString(dataColumn);
                if (!deleted.contains(data)){
                    result.add(data);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    // Gets height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }




    @Override
    protected void onResume() {
        super.onResume();
        images = getCameraImages(new ArrayList<String>());
        boolean swipeActivityFavoritesChanges;
        swipeActivityFavoritesChanges = sharedPreferences.getBoolean("swipe_activity_favorites_changes", false);



        isPlus = sharedPreferences.getBoolean("isPlus", true);

        if (isPlus){
            squareBtn.setImageResource(R.drawable.stop_painted);
            plusMinus.setImageResource(R.drawable.plus_painted);
        }else{
            squareBtn.setImageResource(R.drawable.stop_empty);
            plusMinus.setImageResource(R.drawable.plus_empty);
        }

        boolean isLastItemInImagesEqualsToLastMediaUril = false;
        if (images.size() > 0){
            isLastItemInImagesEqualsToLastMediaUril = images.get(0).equals(lastMediaUril);
        }

        // Second part of conditional statement is:
        //                  Check if the last media file in the list has changed
        if (deletedItemsInSwipeActivity
                || !isLastItemInImagesEqualsToLastMediaUril
                || swipeActivityFavoritesChanges) {

            reloadRecyclerView(columnsInPortrait, columnsInLandscape);
            deletedItemsInSwipeActivity = false;

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("swipe_activity_favorites_changes", false);
            editor.commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        toolbar = (LinearLayout) findViewById(R.id.double_toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.app_skin));


        statusBar = findViewById(R.id.statusBarBackground);
        statusBar.getLayoutParams().height = getStatusBarHeight();
        statusBar.setBackgroundColor(getResources().getColor(R.color.app_skin));

        filterTab = (LinearLayout) findViewById(R.id.filter_tab);
        filterTab.setBackgroundColor(getResources().getColor(R.color.app_skin));
        squareCounterView = (TextView) findViewById(R.id.squareCounter);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        params.topMargin = getStatusBarHeight();
        toolbar.setLayoutParams(params);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isPlus = sharedPreferences.getBoolean("isPlus", true);

        isDeleteMode = sharedPreferences.getBoolean("isDeleteMode", false);
        // taking saved in sharedPreferences parameters and saving them in fields
        columnsInPortrait = sharedPreferences.getInt("portrait", 3);
        columnsInLandscape = sharedPreferences.getInt("landscape", 5);

        filter = sharedPreferences.getString("filter", "All");
        filterButtonProperState(filter, true);


        info = (TextView) toolbar.findViewById(R.id.info);


        // Filter bar buttons
        ImageButton photoFilter = (ImageButton) findViewById(R.id.photo_filter);
        photoFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filter.equals("Photo")) {
                    info.setText("Photo");
                    filterButtonProperState(filter, false);
                    filter = "Photo";
                    filterButtonServing(filter, v);
                    starBtn.setImageResource(R.drawable.star);
                }
            }
        });
        ImageButton videoFilter = (ImageButton) findViewById(R.id.video_filter);
        videoFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filter.equals("Video")) {
                    info.setText("Video");
                    filterButtonProperState(filter, false);
                    filter = "Video";
                    filterButtonServing(filter, v);
                    starBtn.setImageResource(R.drawable.star);
                }
            }
        });
        ImageButton withoutFilter = (ImageButton) findViewById(R.id.without_filter);
        withoutFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filter.equals("All")) {
                    info.setText("All");
                    filterButtonProperState(filter, false);
                    filter = "All";
                    filterButtonServing(filter, v);
                    starBtn.setImageResource(R.drawable.star);
                }
            }
        });
        final ImageButton favoritesFilter = (ImageButton) findViewById(R.id.favorites_filter);
        favoritesFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!filter.equals("Favorites")) {
                    info.setText("Favorites");
                    filterButtonProperState(filter, false);
                    filter = "Favorites";
                    filterButtonServing(filter, v);
                    starBtn.setImageResource(R.drawable.empty_star);
                }
            }
        });

        ImageButton cameraBtn = (ImageButton) toolbar.findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ServingClass.launchCamera(getApplicationContext());
            }
        });

        ImageButton shareBtn = (ImageButton) toolbar.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServingClass.shareBtnAction(PreviewActivity.this);
            }
        });

        starBtn = (ImageButton) toolbar.findViewById(R.id.starBtn);
        if (filter.equals("Favorites")){
            starBtn.setImageResource(R.drawable.empty_star);
        }
        starBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibilityTo = View.VISIBLE;
                boolean switchStarOn = true;
                if (filter.equals("Favorites")){
                    visibilityTo = View.INVISIBLE;
                    switchStarOn = false;
                }

                ArrayList<String> tempList = new ArrayList<String>(images);
                for(int i=0; i<SivAdapter.checkedItems.size(); i++){
                    int checkedItem = SivAdapter.checkedItems.get(i);
                    String temp = tempList.get(checkedItem);

                    if (!SivAdapter.favoritesUri.contains(temp)) {
                        SivAdapter.favoritesUri.add(temp);
                    }else{
                        if (!switchStarOn){
                            SivAdapter.favoritesUri.remove(temp);

                        }
                    }
                }

                SharedPreferences.Editor editor = sharedPreferences.edit();

                Set<String> favUriSet = new HashSet<>();
                for (int i=0; i<SivAdapter.favoritesUri.size(); i++){
                    favUriSet.add(SivAdapter.favoritesUri.get(i).toString());
                }
                editor.putStringSet("favoritesUri", favUriSet);
                editor.commit();

                for (int i=0; i<recyclerView.getChildCount(); i++){
                    if (recyclerView.getChildAt(i).findViewById(R.id.checkmark).getVisibility() == View.VISIBLE){
                        recyclerView.getChildAt(i).findViewById(R.id.favoritesMark).setVisibility(visibilityTo);
                    }
                }
                    images = getCameraImages(new ArrayList<String>());
                    easyDissmisDeleteMode();
                if(!switchStarOn) {
                    reloadRecyclerView(columnsInPortrait, columnsInLandscape);
                }
            }
        });
        cancelBtn = (ImageButton) toolbar.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easyDissmisDeleteMode();
            }
        });

        trashBtn = (ImageButton) toolbar.findViewById(R.id.trashBtn);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServingClass.trashBtnAction(PreviewActivity.this, -1);
            }
        });

        ImageButton magnifier = (ImageButton) toolbar.findViewById(R.id.magnifier);
        magnifier.setVisibility(View.GONE);
        magnifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        squareBtn = (ImageButton) toolbar.findViewById(R.id.squareBtn);

        if (isPlus){
            squareBtn.setImageResource(R.drawable.stop_painted);
        }else{
            squareBtn.setImageResource(R.drawable.stop_empty);
        }

        squareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlus = ServingClass.squareBtnAction(PreviewActivity.this, v, isPlus, sharedPreferences);
            }
        });


        plusMinus = (ImageButton) toolbar.findViewById(R.id.plusMinus);

        if (isPlus){
            plusMinus.setImageResource(R.drawable.plus_painted);
        }else{
            plusMinus.setImageResource(R.drawable.plus_empty);
            squareCounterView.setText(String.valueOf(++squareCounter));
            squareCounterView.setVisibility(View.VISIBLE);
        }

        plusMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlus = ServingClass.plusMinusBtnAction(PreviewActivity.this, v, isPlus, sharedPreferences);
            }
        });


        ImageButton menu = (ImageButton) toolbar.findViewById(R.id.popupMenu);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];

                // Get the x, y location and store it in the location[] array
                location[0] = (int) v.getX();
                location[1] = (int) v.getY();
                v.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Point point = new Point();
                point.x = location[0];
                point.y = location[1];
                showStatusPopup(PreviewActivity.this, point);
            }
        });


        images = getCameraImages(new ArrayList<String>());
        if (images.size() > 0) {
            lastMediaUril = images.get(0);
        }
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewPreviews);

        Set<String> checkedItemsQuantity = sharedPreferences.getStringSet("checkedItems", new HashSet<String>());
        if (checkedItemsQuantity != null && checkedItemsQuantity.size() != 0){
            info.setText("Checked: "+checkedItemsQuantity.size());
        }

        switchColorAndVisibility();

        reloadRecyclerView(columnsInPortrait, columnsInLandscape);

    }

    // Show popup window
    private void showStatusPopup(final Activity context, Point p) {

//        RelativeLayout viewGroup = (RelativeLayout) context.findViewById(R.id.previewActivityMainLayout);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window, null);

        View mode1x3 = layout.findViewById(R.id.id1x3);
        mode1x3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                reloadRecyclerView(1, 3);
            }
        });

        View mode2x4 = layout.findViewById(R.id.id2x4);
        mode2x4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                reloadRecyclerView(2, 4);
            }
        });

        View mode3x5 = layout.findViewById(R.id.id3x5);
        mode3x5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                reloadRecyclerView(3, 5);
            }
        });

        View mode4x6 = layout.findViewById(R.id.id4x6);
        mode4x6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                reloadRecyclerView(4, 6);
            }
        });

        View tellFriend = layout.findViewById(R.id.tellFriend);
        tellFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ServingClass.shareBtnAction(PreviewActivity.this);
            }
        });

        View help = layout.findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
//
                Intent intent = new Intent();
                intent.setClass(PreviewActivity.this, YouTubeActivity.class);
                intent.putExtra("video", "t21C09JiRc4");
                startActivity(intent);
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
                stringBuilder.append("Device name: "+getDeviceName());
                stringBuilder.append('\n');
                stringBuilder.append("OS version: "+android.os.Build.VERSION.RELEASE);
                stringBuilder.append('\n');
                stringBuilder.append("Application version: "+version);



                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","santa_claus@laplandia.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SwipeIV Customer Feedback");
                emailIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        View review = layout.findViewById(R.id.review);
        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                ServingClass.shareBtnAction(PreviewActivity.this);
            }
        });

        popupWindow = new PopupWindow(context);
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

    private void filterButtonServing(String filter, View view){
        images = getCameraImages(new ArrayList<String>());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("filter", filter);
        editor.commit();

        int imageOn;

        switch (filter){
            case "Photo":
                imageOn = R.drawable.photo_on;
                break;
            case "Video":
                imageOn = R.drawable.video_on;
                break;
            case "All":
                imageOn = R.drawable.all_on;
                break;
            case "Favorites":
                imageOn = R.drawable.favorites_on;
                break;
            default:
                imageOn = R.drawable.all_on;
                break;
        }

        ((ImageButton)view).setImageResource(imageOn);

        easyDissmisDeleteMode();
        reloadRecyclerView(columnsInPortrait, columnsInLandscape);
    }

// check or uncheck filter tab buttons
    private void filterButtonProperState(String filter, boolean check){
        ImageButton btn;
        int imageOn;
        int imageOff;
//        boolean isFavorites = false;
        switch (filter){
            case "Photo":
                btn = (ImageButton) this.findViewById(R.id.photo_filter);
                imageOn = R.drawable.photo_on;
                imageOff = R.drawable.photo_off;
                break;
            case "Video":
                btn = (ImageButton) this.findViewById(R.id.video_filter);
                imageOn = R.drawable.video_on;
                imageOff = R.drawable.video_off;
                break;
            case "All":
                btn = (ImageButton) this.findViewById(R.id.without_filter);
                imageOn = R.drawable.all_on;
                imageOff = R.drawable.all_off;
                break;
            case "Favorites":
                btn = (ImageButton) this.findViewById(R.id.favorites_filter);
                imageOn = R.drawable.favorites_on;
                imageOff = R.drawable.favorites_off;
//                isFavorites = true;
                break;
            default:
                btn = (ImageButton) this.findViewById(R.id.without_filter);
                imageOn = R.drawable.all_on;
                imageOff = R.drawable.all_off;
                break;
        }
        if (check){
            btn.setImageResource(imageOn);
//            if(isFavorites){
//                toolbar.setBackgroundColor(getResources().getColor(R.color.favorites_skin));
//                statusBar.setBackgroundColor(getResources().getColor(R.color.favorites_skin));
//                Toast.makeText(this, "isFav", Toast.LENGTH_SHORT).show();
//            }else{
//                toolbar.setBackgroundColor(getResources().getColor(R.color.app_skin));
//                statusBar.setBackgroundColor(getResources().getColor(R.color.app_skin));
//            }
        }else{
            btn.setImageResource(imageOff);
        }

    }

    private void switchColorAndVisibility(){

        if (filter.equals("Favorites")){
            filterTab.setBackgroundColor(getResources().getColor(R.color.favorites_skin));
            toolbar.setBackgroundColor(getResources().getColor(R.color.favorites_skin));
            statusBar.setBackgroundColor(getResources().getColor(R.color.favorites_skin));
        }else{
            filterTab.setBackgroundColor(getResources().getColor(R.color.app_skin));
            toolbar.setBackgroundColor(getResources().getColor(R.color.app_skin));
            statusBar.setBackgroundColor(getResources().getColor(R.color.app_skin));
        }

        if (isDeleteMode) {
            trashBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            starBtn.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(getResources().getColor(R.color.deleteModeColor));
            statusBar.setBackgroundColor(getResources().getColor(R.color.deleteModeColor));
        }else{
            trashBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            starBtn.setVisibility(View.GONE);
//            toolbar.setBackgroundColor(getResources().getColor(R.color.app_skin));
//            statusBar.setBackgroundColor(getResources().getColor(R.color.app_skin));
        }

    }

    public void handleToolbarChanges(boolean delete){
        isDeleteMode = delete;
        switchColorAndVisibility();
    }


    // reloads recyclerView with new options
    public void reloadRecyclerView(int columnsInPortrait, int columnsInLandscape) {

        this.columnsInPortrait = columnsInPortrait;
        this.columnsInLandscape = columnsInLandscape;
        // saving new recycler view parameters to sharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("portrait", columnsInPortrait);
        editor.putInt("landscape", columnsInLandscape);
        editor.commit();

        // Recognition of what orientation is now and getting current screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int imagePreviewSize; // for size of preview miniature
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            imagePreviewSize = size.x / columnsInPortrait;
            recyclerView.setLayoutManager(new GridLayoutManager(this, columnsInPortrait));
        } else {
            imagePreviewSize = size.x / columnsInLandscape;
            recyclerView.setLayoutManager(new GridLayoutManager(this, columnsInLandscape));
        }

        // Instantiation of new recyclerView adapter  //, manager
        adapter = new SivAdapter(this, images, imagePreviewSize);
        recyclerView.setAdapter(adapter);

    }
// function for cancel btn without reloading recyclerview
    public void easyDissmisDeleteMode(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        isDeleteMode = false;
        Set<String> set = new HashSet<>();
        editor.putStringSet("checkedItems", set);
        editor.putBoolean("isDeleteMode", false);
        editor.commit();
        switchColorAndVisibility();
//        info.setText(getResources().getString(R.string.gallery));
        info.setText(filter);

        SivAdapter.checkedItems = new ArrayList<Integer>();

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(1.0f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        for (int i=0; i<recyclerView.getChildCount(); i++){
            View v = recyclerView.getChildAt(i);
            ImageView miniature = (ImageView) v.findViewById(R.id.miniature);
            ImageView checkmark = (ImageView) v.findViewById(R.id.checkmark);
            checkmark.setVisibility(View.INVISIBLE);
            miniature.setColorFilter(filter);
        }
    }

    public void dissmisDeleteMode(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        isDeleteMode = false;
        Set<String> set = new HashSet<>();
        editor.putStringSet("checkedItems", set);
        editor.putBoolean("isDeleteMode", false);
        editor.commit();
        switchColorAndVisibility();
//        info.setText(getResources().getString(R.string.gallery));
        info.setText(filter);
        reloadRecyclerView(columnsInPortrait, columnsInLandscape);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    class DeleteTask extends AsyncTask<ArrayList<File>, Void, Void>{

        @Override
        protected Void doInBackground(ArrayList<File>... params) {
            int size = params[0].size();
            for (int i=0; i<size; i++) {
                deleteFileFromMediaStore(getContentResolver(), params[0].get(i));

            }
            return null;
        }

    }


}
