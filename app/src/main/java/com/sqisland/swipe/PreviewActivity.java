package com.sqisland.swipe;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



public class PreviewActivity extends AppCompatActivity {

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
    protected ImageButton squareBtn;
    protected ImageButton plusMinus;
    protected static String filter;
    private int columnsInPortrait;
    private int columnsInLandscape;
    protected static ImageButton starBtn;
    private LinearLayout filterTab;
    protected TextView squareCounterView;


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

        set = new HashSet<>();
        Set<String> favUriSet = new HashSet<>();
        for (int i=0; i<SivAdapter.favoritesUri.size(); i++){
            favUriSet.add(SivAdapter.favoritesUri.get(i).toString());
        }

        App.editor.putStringSet("checkedItems", set);
        App.editor.putStringSet("favoritesUri", favUriSet);
        App.editor.commit();

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
        String toast = "";
        if (filter.equals("Photo")){
            toast = "Photo";
            info.setText(toast);
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
//                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        }else if (filter.equals("Video")){
            toast = "Video";
            info.setText(toast);
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        }else if (filter.equals("All")){
            toast = "All";
            info.setText(toast);
            selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                    + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        }else if (filter.equals("Favorites")){
            toast="Favorites";
            info.setText(toast);
            return SivAdapter.favoritesUri;
        }

        ServingClass.showExtremelyShortToast(this, toast);

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

                    // For additional sdcard
                    String dir_pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                    if (data.contains("sdcard1") && dir_pic.contains("sdcard0")){

                        String dir_dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
                        String dir_movies = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString();
                        dir_pic = dir_pic.replaceFirst("sdcard0", "sdcard1");
                        dir_dcim = dir_dcim.replaceFirst("sdcard0", "sdcard1");
                        dir_movies = dir_movies.replaceFirst("sdcard0", "sdcard1");

                        if (data.startsWith(dir_pic) || data.startsWith(dir_dcim) || data.startsWith(dir_movies)){
                            result.add(data);
                        }
                    }
//                    data.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (data.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString())
                            || data.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString())
                            || data.startsWith(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString())) {
                        result.add(data);
                    }
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        images = getCameraImages(new ArrayList<String>());
        boolean swipeActivityFavoritesChanges;
        swipeActivityFavoritesChanges = sharedPreferences.getBoolean("swipe_activity_favorites_changes", false);

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        isPlus = sharedPreferences.getBoolean("isPlus", true);

        if (isPlus){
            squareBtn.setImageResource(R.drawable.stop_painted);
            plusMinus.setImageResource(R.drawable.plus_empty);

            squareBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.small_square_plus);
            squareBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.small_square_plus);
            plusMinus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
            plusMinus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

            squareCounterView.setVisibility(View.GONE);
        }else{
            squareBtn.setImageResource(R.drawable.stop_empty);
            plusMinus.setImageResource(R.drawable.plus_painted);

            plusMinus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.small_square_plus);
            plusMinus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.small_square_plus);
            squareBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
            squareBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

            squareCounterView.setVisibility(View.VISIBLE);
            squareCounterView.setText(String.valueOf(ServingClass.squareCounter));
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

            App.editor.putBoolean("swipe_activity_favorites_changes", false);
            App.editor.commit();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);



        toolbar = (LinearLayout) findViewById(R.id.double_toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_skin));


        statusBar = findViewById(R.id.statusBarBackground);
        statusBar.getLayoutParams().height = ServingClass.getStatusBarHeight(this);
        statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_skin));

        filterTab = (LinearLayout) findViewById(R.id.filter_tab);
        filterTab.setBackgroundColor(ContextCompat.getColor(this, R.color.app_skin));
        squareCounterView = (TextView) findViewById(R.id.squareCounter);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        params.topMargin = ServingClass.getStatusBarHeight(this);
        toolbar.setLayoutParams(params);

        sharedPreferences = App.sharedPreferences;
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
                    ServingClass.showExtremelyShortToast(PreviewActivity.this, "Photo");
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
                    ServingClass.showExtremelyShortToast(PreviewActivity.this, "Video");
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
                    ServingClass.showExtremelyShortToast(PreviewActivity.this, "All");
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
                    ServingClass.showExtremelyShortToast(PreviewActivity.this, "Favorites");
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



                Set<String> favUriSet = new HashSet<>();
                for (int i=0; i<SivAdapter.favoritesUri.size(); i++){
                    favUriSet.add(SivAdapter.favoritesUri.get(i).toString());
                }
                App.editor.putStringSet("favoritesUri", favUriSet);
                App.editor.commit();

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
        plusMinus = (ImageButton) toolbar.findViewById(R.id.plusMinus);

        if (isPlus){
            squareBtn.setImageResource(R.drawable.stop_painted);
            plusMinus.setImageResource(R.drawable.plus_empty);
        }else{
            squareBtn.setImageResource(R.drawable.stop_empty);
            plusMinus.setImageResource(R.drawable.plus_painted);
            squareCounterView.setText(String.valueOf(ServingClass.squareCounter));
            squareCounterView.setVisibility(View.VISIBLE);
        }

        squareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlus = ServingClass.squareBtnAction(PreviewActivity.this, v, isPlus, sharedPreferences);
            }
        });

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

                ServingClass.showStatusPopup(PreviewActivity.this, point);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServingClass.temporaryPhones = new ArrayList<>();
        ServingClass.temporaryPhonesIds = new ArrayList<>();
        ServingClass.temporaryPhonesCounter = 0;
    }


    private void filterButtonServing(String filter, View view){
        images = getCameraImages(new ArrayList<String>());

        App.editor.putString("filter", filter);
        App.editor.commit();

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
            filterTab.setBackgroundColor(ContextCompat.getColor(this, R.color.favorites_skin));
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.favorites_skin));
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.favorites_skin));
        }else{
            filterTab.setBackgroundColor(ContextCompat.getColor(this, R.color.app_skin));
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_skin));
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.app_skin));
        }

        if (isDeleteMode) {
            trashBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            starBtn.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.deleteModeColor));
            statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.deleteModeColor));
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
        App.editor.putInt("portrait", columnsInPortrait);
        App.editor.putInt("landscape", columnsInLandscape);
        App.editor.commit();

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
        isDeleteMode = false;
        Set<String> set = new HashSet<>();
        App.editor.putStringSet("checkedItems", set);
        App.editor.putBoolean("isDeleteMode", false);
        App.editor.commit();
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
        isDeleteMode = false;
        Set<String> set = new HashSet<>();
        App.editor.putStringSet("checkedItems", set);
        App.editor.putBoolean("isDeleteMode", false);
        App.editor.commit();
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
