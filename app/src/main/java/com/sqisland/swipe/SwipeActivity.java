package com.sqisland.swipe;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.ArrayList;

public class SwipeActivity extends Activity {

    ArrayList<String> images;
    int width;
    int height;

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
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                SubsamplingScaleImageView view = (SubsamplingScaleImageView) viewPager.findViewWithTag("image_" + position);
                view.resetScaleAndCenter();
            }
        });
    }

    // Adapter for ViewPager
    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LinearLayout llImage = (LinearLayout) getLayoutInflater().inflate(R.layout.view_pager_item, null);

            SubsamplingScaleImageView draweeView = (SubsamplingScaleImageView) llImage.findViewById(R.id.imageFullScreen);
            draweeView.setImage(ImageSource.uri(images.get(position)));
            draweeView.setTag("image_" + position);
            container.addView(llImage, 0);
            return llImage;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}