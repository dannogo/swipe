package com.sqisland.swipe;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
    private ArrayList<Integer> checkedItems = new ArrayList<Integer>();
    private SharedPreferences sharedPreferences;
    boolean deleteMode = false;

    public SivAdapter(Context context, ArrayList<String> images, int imagePreviewSize, FragmentManager manager) {
        this.inflater = LayoutInflater.from(context);
        this.images = images;
        this.context = context;
        this.imagePreviewSize = imagePreviewSize;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> set = sharedPreferences.getStringSet("checkedItems", new HashSet<String>());
        if (set.size()>0){
            this.deleteMode = true;
        }
        Iterator iter = set.iterator();
        while(iter.hasNext()){
            checkedItems.add(Integer.parseInt((String) iter.next()));
        }
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_row, parent, false);
        // setting size of miniature
        view.getLayoutParams().height = imagePreviewSize;
        view.getLayoutParams().width = imagePreviewSize;
        view.requestLayout();

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Uri uri = Uri.parse("file://" + images.get(position));
        //setting thumbnail and image to ImageView
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

        public MyViewHolder(View itemView) {
            super(itemView);

            miniature = (ImageView) itemView.findViewById(R.id.miniature);
            checkmark = (ImageView) itemView.findViewById(R.id.checkmark);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void checkItem(int position, boolean check){
            if (check) {
                checkedItems.add(position);
                deleteMode = true;
                ((PreviewActivity)context).handleTollbarChanges(true);
                ((PreviewActivity) context).setTitle("Checked: " + checkedItems.size());
            }else{
                checkedItems.remove(new Integer(getPosition()));
                if (checkedItems.size() == 0){
                    deleteMode = false;
                    ((PreviewActivity)context).handleTollbarChanges(false);
                    ((PreviewActivity)context).setTitle(context.getResources().getString(R.string.gallery));
                }else{
                    ((PreviewActivity) context).setTitle("Checked: " + checkedItems.size());
                }
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("isDeleteMode", deleteMode);

            Set<String> set = new HashSet<>();
            for (int i=0; i<checkedItems.size(); i++){
                set.add(checkedItems.get(i).toString());
            }
            editor.putStringSet("checkedItems", set);
            editor.commit();
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

        @Override
        public boolean onLongClick(View v) {
            if (!checkedItems.contains(getPosition())) {
                checkItem(getPosition(),true);
                changeCheckedState(v, true);
            }else{
                checkItem(getPosition(), false);
                changeCheckedState(v, false);
            }


            return true;
        }
    }
}
