package com.sqisland.swipe;

import android.app.Activity;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.software.shell.fab.ActionButton;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public class SwipeActivity extends Activity{

    ArrayList<String> images;
    ViewPager viewPager = null;
    ImagePagerAdapter adapter;
//    ImagePagerAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_swipe);

        // Getting position of chosen image from Intent
        Bundle extras = getIntent().getExtras();

        int position = 0;
        if (extras != null) {
            position = extras.getInt("position");
            images = extras.getStringArrayList("images");
        }


        // Preparing viewPager
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
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
            }
        });

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
            Glide.with(container.getContext()).load(Uri.parse("file://" +images.get(position))).thumbnail(0.1f).into(draweeView);

            draweeView.setTag("image_" + position);
            com.software.shell.fab.ActionButton fab = (ActionButton) rlImage.findViewById(R.id.action_button);

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