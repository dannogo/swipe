package com.sqisland.swipe;

import android.content.Intent;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
    private android.support.v7.widget.Toolbar toolbar;
    public int currentPosition;


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

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.swipe_bar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.INVISIBLE);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

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
        getSupportActionBar().setTitle(simplifyImageName(images, currentPosition));
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
                getSupportActionBar().setTitle(simplifyImageName(images, position));

                com.software.shell.fab.ActionButton fab = (ActionButton) viewPager.findViewWithTag("fab_"+position);
                if (fab != null) {
                    if (adapter.isEditMode) {
                        fab.hide();
                    } else {
                        fab.show();
                    }
                }

            }
        });

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

        if (id == R.id.share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.google.com");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
            return true;
        }
        if (id == R.id.remove) {
            RemoveConfirmationDialog dialog = new RemoveConfirmationDialog();
            Bundle data = new Bundle();
            data.putString("purpose", "SwipeActivity");
            data.putInt("position", currentPosition);
            dialog.setArguments(data);
            dialog.show(getFragmentManager(), "Confirmation");
            return true;
        }

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

        boolean isEditMode = false;

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
            final com.software.shell.fab.ActionButton fab = (ActionButton) rlImage.findViewById(R.id.action_button);
            fab.setTag("fab_" + position);
            fab.setOnClickListener(new View.OnClickListener() {
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

            PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);
            mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v1) {
                    if (!isEditMode) {
                        toolbar.setVisibility(View.VISIBLE);
                        fab.hide();
                        isEditMode = true;
                    } else {
                        toolbar.setVisibility(View.INVISIBLE);
                        fab.show();
                        isEditMode = false;
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