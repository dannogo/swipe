package com.sqisland.swipe;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class PreviewActivity extends ActionBarActivity {

    private RecyclerView recyclerView;
    private SivAdapter adapter;
    private ArrayList<String> images;
    protected static boolean deletedItemsInSwipeActivity = false;
    private SharedPreferences sharedPreferences;
    private FragmentManager manager = getFragmentManager(); // needs for confirmation Dialog
    private android.support.v7.widget.Toolbar toolbar;
    private boolean isDeleteMode;

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
                MediaStore.Files.FileColumns.DATA + "=?", new String[] {canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }



    public void deleteChecked(){
        ArrayList<String>  urisForDeleting = new ArrayList<>();
        ArrayList<File> filesForDeleting = new ArrayList<>();
        Set<String> set = sharedPreferences.getStringSet("checkedItems", new HashSet<String>());
        Iterator iter = set.iterator();
        while(iter.hasNext()){
            String str = images.get(Integer.parseInt((String) iter.next()));
            filesForDeleting.add(new File(str));
            urisForDeleting.add(str);
        }
        images = getCameraImages(this, urisForDeleting);
        DeleteTask deleteTask = new DeleteTask();
        deleteTask.execute(filesForDeleting);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int columnsInPortrait = sharedPreferences.getInt("portrait", 4);
        int columnsInLandscape = sharedPreferences.getInt("landscape", 6);
        reloadRecyclerView(columnsInPortrait, columnsInLandscape);
    }

    // gets list of photo's uris. Second argument helps to avoid rendering just deleted photo
    public ArrayList<String> getCameraImages(Context context, ArrayList<String> deleted) {
        // Get relevant columns for use later.
        String[] projection = {
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
        };

// Return only video and image metadata.
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

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
        if (deletedItemsInSwipeActivity) {
            images = getCameraImages(this, new ArrayList<String>());
            int columnsInPortrait = sharedPreferences.getInt("portrait", 4);
            int columnsInLandscape = sharedPreferences.getInt("landscape", 6);
            reloadRecyclerView(columnsInPortrait, columnsInLandscape);

            deletedItemsInSwipeActivity = false;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        images = getCameraImages(this, new ArrayList<String>());
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewPreviews);

        Set<String> checkedItemsQuantity = sharedPreferences.getStringSet("checkedItems", new HashSet<String>());
        if (checkedItemsQuantity != null && checkedItemsQuantity.size() != 0){
            setTitle("Checked: "+checkedItemsQuantity.size());
        }
        isDeleteMode = sharedPreferences.getBoolean("isDeleteMode", false);
        // taking saved in sharedPreferences parameters and saving them in fields
        int columnsInPortrait = sharedPreferences.getInt("portrait", 4);
        int columnsInLandscape = sharedPreferences.getInt("landscape", 6);


        reloadRecyclerView(columnsInPortrait, columnsInLandscape);
        this.invalidateOptionsMenu();
    }


    public void handleTollbarChanges(boolean delete){
        isDeleteMode = delete;
        this.invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
            MenuItem delete = menu.findItem(R.id.delete);
            MenuItem cancel = menu.findItem(R.id.cancel);
        if (isDeleteMode) {
            delete.setVisible(true);
            cancel.setVisible(true);
            toolbar.setBackgroundColor(getResources().getColor(R.color.deleteModeColor));
        }else{
            delete.setVisible(false);
            cancel.setVisible(false);
            toolbar.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        }
        return true;
    }

    // reloads recyclerView with new options
    public void reloadRecyclerView(int columnsInPortrait, int columnsInLandscape) {

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

        // Instantiation of new recyclerView adapter
        adapter = new SivAdapter(this, images, imagePreviewSize, manager);
        recyclerView.setAdapter(adapter);

    }

    public void dissmisDeleteMode(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        isDeleteMode = false;
        Set<String> set = new HashSet<String>();
        editor.putStringSet("checkedItems", set);
        editor.putBoolean("isDeleteMode", false);
        editor.commit();
        this.invalidateOptionsMenu();
        setTitle(getResources().getString(R.string.gallery));
        int columnsInPortrait = sharedPreferences.getInt("portrait", 4);
        int columnsInLandscape = sharedPreferences.getInt("landscape", 6);
        reloadRecyclerView(columnsInPortrait, columnsInLandscape);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        // setting new recyclerView parameters
        switch (item.getItemId()) {
            case R.id.pl1x3:
                reloadRecyclerView(1, 3);
                return true;
            case R.id.pl2x4:
                reloadRecyclerView(2, 4);
                return true;
            case R.id.pl3x5:
                reloadRecyclerView(3, 5);
                return true;
            case R.id.pl4x6:
                reloadRecyclerView(4, 6);
                return true;

        }

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.camera) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(intent, 1);
            return true;
        }

        if (id == R.id.share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.google.com");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }

        if (id == R.id.cancel){
            dissmisDeleteMode();
        }
        if (id == R.id.delete){

            RemoveConfirmationDialog dialog = new RemoveConfirmationDialog();
            Bundle data = new Bundle();
            data.putString("purpose", "PreviewActivity");
            dialog.setArguments(data);
            dialog.show(getFragmentManager(), "Confirmation");

        }

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
