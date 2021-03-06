package com.sqisland.swipe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.software.shell.fab.ActionButton;

import java.io.File;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class SwipeActivity extends AppCompatActivity{

    ArrayList<String> images;
    ViewPager viewPager = null;
    ImagePagerAdapter adapter;
    private LinearLayout toolbar;
    private View statusBar;
    private TextView toolbarTitle;
    public int currentPosition;
    private Context context;
    private static boolean isEditMode = false;
    protected TextView squareCounterView;
    protected ImageButton squareBtn;
    protected ImageButton plusMinus;
    protected TextView squareDescription, plusLeftDescription, plusBottomDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_swipe);

        context = this;
        toolbarTitle = (TextView) findViewById(R.id.info);

        toolbar = (LinearLayout) findViewById(R.id.double_toolbar);
        squareCounterView = (TextView) findViewById(R.id.squareCounter);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            statusBar = findViewById(R.id.statusBarBackground);
            statusBar.getLayoutParams().height = ServingClass.getStatusBarHeight(this);
            statusBar.setBackgroundColor(Color.parseColor("#1A237E"));

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = ServingClass.getStatusBarHeight(this);
            toolbar.setLayoutParams(params);
        }

        // Getting position of chosen image from Intent
        Bundle extras = getIntent().getExtras();

        currentPosition = 0;
        if (extras != null) {
            currentPosition = extras.getInt("position");
            images = extras.getStringArrayList("images");
        }


        ImageButton starInToolbar = (ImageButton) toolbar.findViewById(R.id.starBtn);
        if (SivAdapter.favoritesUri.contains(images.get(currentPosition))) {
            starInToolbar.setVisibility(View.GONE);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.favorites_skin));
            if (statusBar != null) {
                statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.favorites_skin));
            }
        }else{
            starInToolbar.setVisibility(View.VISIBLE);
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_skin));
            if (statusBar != null) {
                statusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.toolbar_skin));
            }
        }
        starInToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoritesConfirmation confirmation = new FavoritesConfirmation();
                Bundle data = new Bundle();
                data.putInt("position", currentPosition);
                confirmation.setArguments(data);
                confirmation.show(getFragmentManager(), "FavoritesConfirmation");

            }
        });


        // Preparing viewPager
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin(30);
        viewPager.setPageMarginDrawable(R.color.primaryColorDark);
        viewPager.setCurrentItem(currentPosition);
        toolbarTitle.setText(simplifyImageName(images, currentPosition));

        if (isEditMode) {
            if (statusBar != null) {
                statusBar.setVisibility(View.VISIBLE);
            }
            toolbar.setVisibility(View.VISIBLE);
        } else {
            if (statusBar != null) {
                statusBar.setVisibility(View.INVISIBLE);
            }
            toolbar.setVisibility(View.INVISIBLE);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

                ImageView starInToolbar = (ImageView) toolbar.findViewById(R.id.starBtn);
                if (SivAdapter.favoritesUri.contains(images.get(position))) {
                    starInToolbar.setVisibility(View.GONE);
                    toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.favorites_skin));
                    if (statusBar != null) {
                        statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.favorites_skin));
                    }
                } else {
                    starInToolbar.setVisibility(View.VISIBLE);
                    toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar_skin));
                    if (statusBar != null) {
                        statusBar.setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar_skin));
                    }
                }

                ImageButton fabTrash = (ImageButton) viewPager.findViewWithTag("fab_trash_" + position);
                ImageButton fabCamera = (ImageButton) viewPager.findViewWithTag("fab_camera_" + position);
                ImageButton fabMagnifier = (ImageButton) viewPager.findViewWithTag("fab_magnifier_" + position);

                if (fabTrash != null) {
                    if (isEditMode) {
                        fabTrash.setVisibility(View.INVISIBLE);
                        fabMagnifier.setVisibility(View.INVISIBLE);
                        fabCamera.setVisibility(View.INVISIBLE);
                    } else {
                        fabTrash.setVisibility(View.VISIBLE);
                        fabMagnifier.setVisibility(View.VISIBLE);
                        fabCamera.setVisibility(View.VISIBLE);
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
                ServingClass.launchCamera(context);
            }
        });

        ImageButton shareBtn = (ImageButton) toolbar.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServingClass.shareBtnAction(SwipeActivity.this);
            }
        });

        ImageButton trashBtn = (ImageButton) toolbar.findViewById(R.id.trashBtn);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServingClass.trashBtnAction(SwipeActivity.this, currentPosition);

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

                ServingClass.showStatusPopup(SwipeActivity.this, point);
            }
        });

        ImageButton magnifier = (ImageButton) toolbar.findViewById(R.id.magnifier);
        magnifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Magnifier", Toast.LENGTH_SHORT).show();
            }
        });

        squareBtn = (ImageButton) toolbar.findViewById(R.id.squareBtn);
        plusMinus = (ImageButton) toolbar.findViewById(R.id.plusMinus);
        squareDescription = (TextView) toolbar.findViewById(R.id.squareDescription);
        plusLeftDescription = (TextView) toolbar.findViewById(R.id.plusLeftDescription);
        plusBottomDescription = (TextView) toolbar.findViewById(R.id.plusBottomDescription);

        render(ServingClass.Btn.NONE);

        squareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                render(ServingClass.Btn.SQUARE);
            }
        });

        plusMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                render(ServingClass.Btn.PLUS);
            }
        });

        ImageButton cancelBtn = (ImageButton) findViewById(R.id.cancelBtn);
        cancelBtn.setVisibility(View.GONE);


    }

    private void render(ServingClass.Btn btn){
        if (btn == ServingClass.Btn.NONE){
            if (App.isPlus){
                squareBtn.setVisibility(View.INVISIBLE);
                squareDescription.setVisibility(View.INVISIBLE);
                plusLeftDescription.setVisibility(View.INVISIBLE);
                plusBottomDescription.setVisibility(View.VISIBLE);
                plusBottomDescription.getLayoutParams().height = (int) getResources().getDimension(R.dimen.text_height_plus_bottom_desc);
//                squareBtn.setImageResource(R.drawable.stop_painted);
                plusMinus.setImageResource(R.drawable.plus_empty);

                squareBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.small_square_plus);
                squareBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.small_square_plus);
                plusMinus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                plusMinus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

            }else{
                squareBtn.setVisibility(View.VISIBLE);
                squareDescription.setVisibility(View.VISIBLE);
                plusLeftDescription.setVisibility(View.VISIBLE);
                plusBottomDescription.setVisibility(View.INVISIBLE);
                plusBottomDescription.getLayoutParams().height = (int) getResources().getDimension(R.dimen.zero_text_height_plus_bottom_desc);
//                squareBtn.setImageResource(R.drawable.stop_empty);
                plusMinus.setImageResource(R.drawable.plus_painted);
                squareCounterView.setText(String.valueOf(ServingClass.squareCounter));
                squareCounterView.setVisibility(View.VISIBLE);

                plusMinus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.small_square_plus);
                plusMinus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.small_square_plus);
                squareBtn.getLayoutParams().height = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                squareBtn.getLayoutParams().width = (int) getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

            }
        }else if (btn == ServingClass.Btn.SQUARE){
            if (!App.isPlus){
                squareBtn.setVisibility(View.INVISIBLE);
                squareDescription.setVisibility(View.INVISIBLE);
                plusLeftDescription.setVisibility(View.INVISIBLE);
                plusBottomDescription.setVisibility(View.VISIBLE);
                plusBottomDescription.getLayoutParams().height = (int) getResources().getDimension(R.dimen.text_height_plus_bottom_desc);
//                squareBtn.setImageResource(R.drawable.stop_painted);
                ServingClass.squareCounter = 1;
                plusMinus.setImageResource(R.drawable.plus_empty);

                squareCounterView.setVisibility(View.GONE);
                squareBtn.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                squareBtn.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                plusMinus.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                plusMinus.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);

                App.isPlus = true;
                App.editor.putBoolean("isPlus", App.isPlus);
                App.editor.commit();
            }
        }else if (btn == ServingClass.Btn.PLUS){
            if (App.isPlus){
                plusMinus.setImageResource(R.drawable.plus_painted);
                squareBtn.setVisibility(View.VISIBLE);
                squareDescription.setVisibility(View.VISIBLE);
                plusLeftDescription.setVisibility(View.VISIBLE);
                plusBottomDescription.setVisibility(View.INVISIBLE);
                plusBottomDescription.getLayoutParams().height = (int) getResources().getDimension(R.dimen.zero_text_height_plus_bottom_desc);
//                squareBtn.setImageResource(R.drawable.stop_empty);

                squareCounterView.setText(String.valueOf(ServingClass.squareCounter));
                squareCounterView.setVisibility(View.VISIBLE);

                squareBtn.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                squareBtn.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.large_icon_size_in_toolbar);
                plusMinus.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.small_square_plus);
                plusMinus.getLayoutParams().width = (int) context.getResources().getDimension(R.dimen.small_square_plus);

                App.isPlus = false;
                App.editor.putBoolean("isPlus", false);
                App.editor.commit();
            }else{
                squareCounterView.setText(String.valueOf(++ServingClass.squareCounter));
            }
        }
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
            ImageView star = (ImageView) rlImage.findViewById(R.id.starInSingle);
            star.setTag("star_" + position);

            final String imagePath = images.get(position);
            if (SivAdapter.favoritesUri.contains(imagePath)){
                star.setVisibility(View.VISIBLE);
            }
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
            final ImageButton fabTrash = (ImageButton) rlImage.findViewById(R.id.trash_float);
            fabTrash.setTag("fab_trash_" + position);

            fabTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServingClass.trashBtnAction(SwipeActivity.this, position);
                }
            });

            final ImageButton fabCamera = (ImageButton) rlImage.findViewById(R.id.camera_float);
            fabCamera.setTag("fab_camera_" + position);
            fabCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServingClass.launchCamera(SwipeActivity.this);
                }
            });

            final ImageButton fabMagnifier = (ImageButton) rlImage.findViewById(R.id.magnifier_float);
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
                        if (statusBar != null) {
                            statusBar.setVisibility(View.VISIBLE);
                        }

                        fabTrash.setVisibility(View.INVISIBLE);
                        fabMagnifier.setVisibility(View.INVISIBLE);
                        fabCamera.setVisibility(View.INVISIBLE);
                        SwipeActivity.isEditMode = true;
                    } else {
                        toolbar.setVisibility(View.INVISIBLE);
                        if (statusBar != null) {
                            statusBar.setVisibility(View.INVISIBLE);
                        }

                        fabTrash.setVisibility(View.VISIBLE);
                        fabMagnifier.setVisibility(View.VISIBLE);
                        fabCamera.setVisibility(View.VISIBLE);

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