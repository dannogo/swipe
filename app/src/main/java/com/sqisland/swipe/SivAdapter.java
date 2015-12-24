package com.sqisland.swipe;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by oleh on 6/21/15.
 */
public class SivAdapter extends RecyclerView.Adapter<SivAdapter.MyViewHolder> {

    int imagePreviewSize;
    private LayoutInflater inflater;
    private Context context;
    private ArrayList<String> images;
    protected static ArrayList<Integer> checkedItems = new ArrayList<Integer>();
    protected static ArrayList<String> favoritesUri = new ArrayList<String>();
    boolean deleteMode = false;
    RecyclerView recyclerView;
                                                                    //, FragmentManager manager
    public SivAdapter(final Context context, final ArrayList<String> images, int imagePreviewSize) {
        this.inflater = LayoutInflater.from(context);
        this.images = images;
        this.context = context;
        this.imagePreviewSize = imagePreviewSize;

        Set<String> set = App.sharedPreferences.getStringSet("checkedItems", new HashSet<String>());
        if (set.size()>0){
            this.deleteMode = true;
        }
        Iterator iter = set.iterator();
        while(iter.hasNext()){
            String str = (String) iter.next();
            checkedItems.add(Integer.parseInt(str));
        }

        Set<String> favUriSet = App.sharedPreferences.getStringSet("favoritesUri", new HashSet<String>());

        for (String s : favUriSet) {
            if (!favoritesUri.contains(s)) {
                favoritesUri.add(s);
            }
        }

    }


    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_row, parent, false);
        // setting size of miniature
        view.getLayoutParams().height = imagePreviewSize;
        view.getLayoutParams().width = imagePreviewSize;
        view.requestLayout();

        recyclerView = (RecyclerView) parent.findViewById(R.id.recyclerViewPreviews);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String imagePath = images.get(position);
        Uri uri = Uri.parse("file://" + imagePath);

        //setting thumbnail and image to ImageView
        if (imagePath.substring(imagePath.length() - 3, imagePath.length()).equals("mp4")){
                holder.playicon.setVisibility(View.VISIBLE);
        }else{
            holder.playicon.setVisibility(View.INVISIBLE);
        }

        if (App.isPlus){
            holder.smallMagnifier.setVisibility(View.GONE);
        }else{
            holder.smallMagnifier.setVisibility(View.VISIBLE);
        }

        if (favoritesUri.contains(images.get(position))){
            holder.star.setVisibility(View.VISIBLE);
        }else{
            holder.star.setVisibility(View.INVISIBLE);
        }

            Glide.with(context).load(uri).thumbnail(0.05f).into(holder.miniature);
            setItemProperState(holder);
    }

    public void setItemProperState(MyViewHolder holder){

        ColorMatrix matrix = new ColorMatrix();

        holder.checkmark.getLayoutParams().height = imagePreviewSize / 2;
        holder.checkmark.getLayoutParams().width = imagePreviewSize / 2;

        if (checkedItems.contains(holder.getPosition())){
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.miniature.setColorFilter(filter);
            holder.checkmark.setVisibility(View.VISIBLE);
        }else{
            matrix.setSaturation(1.0f);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.miniature.setColorFilter(filter);
            holder.checkmark.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // ViewHolder - for reusing objects while scrolling throw the recyclerView
    protected class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView miniature;
        ImageView checkmark;
        ImageView playicon;
        ImageView smallMagnifier;
        ImageView star;

        public MyViewHolder(final View itemView) {
            super(itemView);

            miniature = (ImageView) itemView.findViewById(R.id.miniature);
            checkmark = (ImageView) itemView.findViewById(R.id.checkmark);
            playicon = (ImageView) itemView.findViewById(R.id.playicon);
            smallMagnifier = (ImageView) itemView.findViewById(R.id.small_magnifier);
            star = (ImageView) itemView.findViewById(R.id.favoritesMark);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            smallMagnifier.setOnClickListener(this);
            smallMagnifier.setOnLongClickListener(this);

        }




        public void checkItem(int position, boolean check){

            if (check) {
                checkedItems.add(position);
                deleteMode = true;
                ((PreviewActivity)context).handleToolbarChanges(true);
                ((PreviewActivity) context).info.setText("Checked: " + checkedItems.size());
            }else{
                checkedItems.remove(new Integer(getPosition()));
                if (checkedItems.size() == 0){
                    deleteMode = false;
                    ((PreviewActivity) context).handleToolbarChanges(false);
                    ((PreviewActivity)context).info.setText(context.getResources().getString(R.string.gallery));
                }else{
                    ((PreviewActivity) context).info.setText("Checked: " + checkedItems.size());
                }
            }

            App.editor.putBoolean("isDeleteMode", deleteMode);

            Set<String> set = new HashSet<>();
            for (int i=0; i<checkedItems.size(); i++){
                set.add(checkedItems.get(i).toString());
            }
            App.editor.putStringSet("checkedItems", set);
            App.editor.commit();
        }

        public void changeCheckedState(View v, boolean check){
            ColorMatrix matrix = new ColorMatrix();
            ImageView miniature = (ImageView) v.findViewById(R.id.miniature);
            ImageView checkmark = (ImageView) v.findViewById(R.id.checkmark);
            if (check){
                matrix.setSaturation(0);
                checkmark.setVisibility(View.VISIBLE);
            }else{
                matrix.setSaturation(1.0f);
                checkmark.setVisibility(View.INVISIBLE);
            }
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            miniature.setColorFilter(filter);
        }



        @Override
        public void onClick(View v) {
            if (v.getId() == smallMagnifier.getId()){
                Toast.makeText(context, "Magnifier", Toast.LENGTH_SHORT).show();

            }else{
                deleteMode = App.sharedPreferences.getBoolean("isDeleteMode", false);
                if (!deleteMode) {
                    // launching Swipe Activity and submitting position of clicked item
                    Intent intent = new Intent();
                    intent.setClass(context, SwipeActivity.class);
                    intent.putExtra("position", getPosition());
                    intent.putStringArrayListExtra("images", SivAdapter.this.images);
                    context.startActivity(intent);
                }else{
                    if (!checkedItems.contains(getPosition())) {
                        checkItem(getPosition(), true);
                        changeCheckedState(v, true);
                    }else{
                        checkItem(getPosition(), false);
                        changeCheckedState(v, false);
                    }
                }
            }
        }


        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == smallMagnifier.getId()){
                Toast.makeText(context, "Magnifier", Toast.LENGTH_SHORT).show();

            }else {
                deleteMode = App.sharedPreferences.getBoolean("isDeleteMode", false);
                if (!checkedItems.contains(getPosition())) {
                    checkItem(getPosition(), true);
                    changeCheckedState(v, true);
                } else {
                    checkItem(getPosition(), false);
                    changeCheckedState(v, false);
                }
            }

            return true;
        }
    }
}
