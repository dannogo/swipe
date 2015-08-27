package com.sqisland.swipe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.software.shell.fab.ActionButton;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class SwipeActivity extends ActionBarActivity{

    ArrayList<String> images;
    ViewPager viewPager = null;
    ImagePagerAdapter adapter;
    private LinearLayout toolbar;
    private View statusBar;
    private TextView toolbarTitle;
    public int currentPosition;
    private Context context;
    private PopupMenu popup;
    private static boolean isEditMode = false;
    private SharedPreferences sharedPreferences;
    private boolean isPlus;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_swipe);

        context = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isPlus = sharedPreferences.getBoolean("isPlus", true);

        toolbarTitle = (TextView) findViewById(R.id.info);

        toolbar = (LinearLayout) findViewById(R.id.double_toolbar);

        statusBar = findViewById(R.id.statusBarBackground);
        statusBar.getLayoutParams().height = getStatusBarHeight();
        statusBar.setBackgroundColor(Color.parseColor("#1A237E"));

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        params.topMargin = getStatusBarHeight();
        toolbar.setLayoutParams(params);

        // Getting position of chosen image from Intent
        Bundle extras = getIntent().getExtras();

        currentPosition = 0;
        if (extras != null) {
            currentPosition = extras.getInt("position");
            images = extras.getStringArrayList("images");
        }


        // Preparing viewPager
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(30);
        viewPager.setPageMarginDrawable(R.color.primaryColorDark);
        viewPager.setCurrentItem(currentPosition);
        toolbarTitle.setText(simplifyImageName(images, currentPosition));

        if (isEditMode) {
            statusBar.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            statusBar.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.INVISIBLE);
        }

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                PhotoView view = (PhotoView) viewPager.findViewWithTag("image_" + position);
                if (view != null) {
                    view.setDisplayMatrix(new Matrix());
                }
                currentPosition = position;

                toolbarTitle.setText(simplifyImageName(images, position));

                com.software.shell.fab.ActionButton fabTrash = (ActionButton) viewPager.findViewWithTag("fab_trash_" + position);
                com.software.shell.fab.ActionButton fabCamera = (ActionButton) viewPager.findViewWithTag("fab_camera_" + position);
                com.software.shell.fab.ActionButton fabMagnifier = (ActionButton) viewPager.findViewWithTag("fab_magnifier_" + position);
                if (fabTrash != null) {
                    if (isEditMode) {
                        fabTrash.hide();
                        fabMagnifier.hide();
                        fabCamera.hide();
                    } else {
                        fabTrash.show();
                        fabMagnifier.show();
                        fabCamera.show();
                    }
                }

            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageButton cameraBtn = (ImageButton) toolbar.findViewById(R.id.cameraBtn);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewActivity.launchCamera(context);
            }
        });

        ImageButton shareBtn = (ImageButton) toolbar.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.google.com");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        ImageButton trashBtn = (ImageButton) toolbar.findViewById(R.id.trashBtn);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveConfirmationDialog dialog = new RemoveConfirmationDialog();
                Bundle data = new Bundle();
                data.putString("purpose", "SwipeActivity");
                data.putInt("position", currentPosition);
                dialog.setArguments(data);
                dialog.show(getFragmentManager(), "Confirmation");
            }
        });

        ImageButton menu = (ImageButton) toolbar.findViewById(R.id.popupMenu);
        popup = new PopupMenu(this, menu);
        MenuInflater menuInflater = popup.getMenuInflater();
        menuInflater.inflate(R.menu.menu_swipe, popup.getMenu());
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });

        ImageButton magnifier = (ImageButton) toolbar.findViewById(R.id.magnifier);
        magnifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Magnifier", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton squareBtn = (ImageButton) toolbar.findViewById(R.id.squareBtn);
        if (isPlus){
            squareBtn.setImageResource(R.drawable.stop_empty);
        }else{
            squareBtn.setImageResource(R.drawable.stop_painted);
        }
        squareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlus){
                    ((ImageButton)v).setImageResource(R.drawable.stop_empty);
                    ((ImageButton)findViewById(R.id.plusMinus)).setImageResource(R.drawable.plus_alone);

                    isPlus = true;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isPlus", true);
                    editor.commit();
                }
            }
        });
        ImageButton plusMinus = (ImageButton) toolbar.findViewById(R.id.plusMinus);
        if (isPlus){
            plusMinus.setImageResource(R.drawable.plus_alone);
        }else{
            plusMinus.setImageResource(R.drawable.minus_alone);
        }
        plusMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlus){
                    ((ImageButton)v).setImageResource(R.drawable.minus_alone);
                    ((ImageButton)findViewById(R.id.squareBtn)).setImageResource(R.drawable.stop_painted);

                    isPlus = false;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isPlus", false);
                    editor.commit();
                }
            }
        });

        ImageButton cancelBtn = (ImageButton) findViewById(R.id.cancelBtn);
        cancelBtn.setVisibility(View.GONE);


    }

    protected static String simplifyImageName(ArrayList<String> images, int position){
        String fullString = images.get(position);
        int length = fullString.length();
        String firstPart = fullString.substring(length - 23, length - 20);
        String secondPart = fullString.substring(length-11);
        StringBuilder builder = new StringBuilder();
        builder.append(firstPart);
        builder.append(secondPart);
        return builder.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_swipe, menu);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void deleteAfterConfirmation(int position){
            DeleteTask deleteTask = new DeleteTask();
            deleteTask.execute(new File(images.get(position)));
            PreviewActivity.deletedItemsInSwipeActivity = true;
            removeView(viewPager, position);
        }

        public int removeView (ViewPager pager, int position)
        {
            pager.setAdapter(null);
            images.remove(position);
            pager.setAdapter(adapter);
            if (position!=0) {
                pager.setCurrentItem(position - 1);
            }

            return position;
        }



    // Adapter for ViewPager
    private class ImagePagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            RelativeLayout rlImage = (RelativeLayout) getLayoutInflater().inflate(R.layout.view_pager_item, null);

            PhotoView draweeView = (PhotoView) rlImage.findViewById(R.id.imageFullScreen);
            draweeView.setMaximumScale(10.0f);

            final String imagePath = images.get(position);
            final Uri uri = Uri.parse("file://" + imagePath);
            ImageView playicon = (ImageView) rlImage.findViewById(R.id.playicon);
            PhotoView photoView = (PhotoView) rlImage.findViewById(R.id.imageFullScreen);

            if (imagePath.substring(imagePath.length() - 3, imagePath.length()).equals("mp4")){
                playicon.setVisibility(View.VISIBLE);
                playicon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setDataAndType(uri, "video/mp4");
                        startActivity(intent);

                    }
                });
            }
            else{
                playicon.setVisibility(View.INVISIBLE);
            }

            Glide.with(container.getContext()).load(uri).thumbnail(0.1f).into(draweeView);

            draweeView.setTag("image_" + position);
            final com.software.shell.fab.ActionButton fabTrash = (ActionButton) rlImage.findViewById(R.id.trash_float);
            fabTrash.setTag("fab_trash_" + position);

            fabTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RemoveConfirmationDialog dialog = new RemoveConfirmationDialog();
                    Bundle data = new Bundle();
                    data.putString("purpose", "SwipeActivity");
                    data.putInt("position", position);
                    dialog.setArguments(data);
                    dialog.show(getFragmentManager(), "Confirmation");
                }
            });

            final com.software.shell.fab.ActionButton fabCamera = (ActionButton) rlImage.findViewById(R.id.camera_float);
            fabCamera.setTag("fab_camera_" + position);
            fabCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreviewActivity.launchCamera(SwipeActivity.this);
                }
            });

            final com.software.shell.fab.ActionButton fabMagnifier = (ActionButton) rlImage.findViewById(R.id.magnifier_float);
            fabMagnifier.setTag("fab_magnifier_" + position);
            fabMagnifier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Magnifier float", Toast.LENGTH_SHORT).show();
                }
            });

            if (SwipeActivity.isEditMode){
                fabCamera.setVisibility(View.INVISIBLE);
                fabMagnifier.setVisibility(View.INVISIBLE);
                fabTrash.setVisibility(View.INVISIBLE);
            }

            PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);
            mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v1) {
                    if (!SwipeActivity.isEditMode) {
                        toolbar.setVisibility(View.VISIBLE);
                        statusBar.setVisibility(View.VISIBLE);
                        fabTrash.hide();
                        fabMagnifier.hide();
                        fabCamera.hide();
                        SwipeActivity.isEditMode = true;
                    } else {
                        toolbar.setVisibility(View.INVISIBLE);
                        statusBar.setVisibility(View.INVISIBLE);
                        fabTrash.show();
                        fabMagnifier.show();
                        fabCamera.show();
                        SwipeActivity.isEditMode = false;
                    }
                }
            });

            container.addView(rlImage, 0);

            return rlImage;

        }



        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }
    }

    class DeleteTask extends AsyncTask<File, Void, Void> {

        @Override
        protected Void doInBackground(File... params) {

                PreviewActivity.deleteFileFromMediaStore(getContentResolver(), params[0]);

            return null;
        }

    }
}