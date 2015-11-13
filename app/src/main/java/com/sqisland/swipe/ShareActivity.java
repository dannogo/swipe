package com.sqisland.swipe;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/**
 * Created by Admin on 13-Nov-15.
 */
public class ShareActivity extends ActionBarActivity {

    private ViewPager sharePager;
    private SlidingTabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        View statusBar = findViewById(R.id.statusBarBackground);
        statusBar.getLayoutParams().height = ServingClass.getStatusBarHeight(this);

        sharePager = (ViewPager) findViewById(R.id.pager);
        sharePager.setAdapter(new SharePagerAdapter(getSupportFragmentManager()));
        tabLayout = (SlidingTabLayout) findViewById(R.id.tabLayout);
        tabLayout.setDistributeEvenly(true);

        tabLayout.setViewPager(sharePager);

    }

    class SharePagerAdapter extends FragmentPagerAdapter{

        String[] tabTitles;

        public SharePagerAdapter(FragmentManager fm) {
            super(fm);
            tabTitles = getResources().getStringArray(R.array.shareTabs);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0){
                fragment = new FragmentSMS();
            }else if (position == 1){
                fragment = new FragmentOther();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
