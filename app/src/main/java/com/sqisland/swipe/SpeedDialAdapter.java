package com.sqisland.swipe;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by oleh on 11/25/15.
 */
public class SpeedDialAdapter extends RecyclerView.Adapter<SpeedDialAdapter.SpeedDialViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    protected ArrayList<String> ids;
    protected ArrayList<String> names;
    protected ArrayList<String> phones;
    protected ArrayList<String> photos;
    int[] speedDialColors;
    private FragmentSMS fragmentSMS;

    public SpeedDialAdapter(Context context, ArrayList<String> ids,
                            ArrayList<String> names, ArrayList<String> phones,
                            ArrayList<String> photos, FragmentSMS fragmentSMS) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.fragmentSMS = fragmentSMS;
        this.ids = new ArrayList<>(ids);
        this.names = new ArrayList<>(names);
        this.phones = new ArrayList<>(phones);
        this.photos = new ArrayList<>(photos);
        speedDialColors = context.getResources().getIntArray(R.array.speed_dial_colors);
    }

    @Override
    public SpeedDialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.speed_dial_row, parent, false);
        SpeedDialViewHolder holder = new SpeedDialViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(SpeedDialViewHolder holder, int position) {
        int colorPosition;
        if (position >= speedDialColors.length){
            colorPosition = speedDialColors[position % speedDialColors.length];
            Log.w("LOG", ""+(position % speedDialColors.length));
        }else{
            colorPosition = speedDialColors[position];
        }
        if (ServingClass.checkedPhones.contains(phones.get(position))){
            holder.speedDialCheckmark.setImageResource(R.drawable.checked_checkbox_50_white);
        }else{
            holder.speedDialCheckmark.setImageResource(R.drawable.unchecked_checkbox_50_white);
        }

        if (photos.get(position) != null){
            holder.speedDialPhoto.setVisibility(View.VISIBLE);
            holder.speedDialFirstCharacter.setVisibility(View.INVISIBLE);
            holder.speedDialPhoto.setImageURI(Uri.parse(photos.get(position)));
        }else{
            holder.speedDialFirstCharacter.setText(names.get(position).substring(0, 1));
        }

        holder.speedDialRowContent.setBackgroundColor(colorPosition);
        holder.speedDialName.setText(names.get(position));
        holder.speedDialPhone.setText(phones.get(position));
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    class SpeedDialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView deleteContactFromSpeedDial, speedDialPhoto, speedDialCheckmark;
        private TextView speedDialFirstCharacter, speedDialName, speedDialPhone;
        private RelativeLayout speedDialRowContent;

        public SpeedDialViewHolder(View itemView) {
            super(itemView);
            deleteContactFromSpeedDial = (ImageView) itemView.findViewById(R.id.deleteContactFromSpeedDial);
            speedDialPhoto = (ImageView) itemView.findViewById(R.id.speedDialPhoto);
            speedDialCheckmark = (ImageView) itemView.findViewById(R.id.speedDialCheckmark);
            speedDialFirstCharacter = (TextView) itemView.findViewById(R.id.speedDialFirstCharacter);
            speedDialName = (TextView) itemView.findViewById(R.id.speedDialName);
            speedDialPhone = (TextView) itemView.findViewById(R.id.speedDialPhone);
            speedDialRowContent = (RelativeLayout) itemView.findViewById(R.id.speedDialRowContent);

            itemView.setOnClickListener(this);
            deleteContactFromSpeedDial.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String currentPhone = speedDialPhone.getText().toString();
            if (view.getId() == deleteContactFromSpeedDial.getId()){
                Toast.makeText(context, "Delete speeddial number", Toast.LENGTH_SHORT).show();
            }else if (view.getId() == itemView.getId()){
                if (!ServingClass.checkedPhones.contains(currentPhone)){
                    ServingClass.checkedPhones.add(currentPhone);
                    speedDialCheckmark.setImageResource(R.drawable.checked_checkbox_50_white);
                }else{
                    ServingClass.checkedPhones.remove(currentPhone);
                    speedDialCheckmark.setImageResource(R.drawable.unchecked_checkbox_50_white);
                }
                // synchronizing checkmarks with contact list
                for (int i=0; i<fragmentSMS.contactList.getChildCount(); i++){
                    if (ServingClass.checkedPhones.contains(((TextView) fragmentSMS.contactList.getChildAt(i).findViewById(R.id.contactDescription)).getText().toString())){
                        ((ImageView)fragmentSMS.contactList.getChildAt(i).findViewById(R.id.contactCheckbox)).setImageResource(R.drawable.checked_checkbox_50);
                    }else{
                        ((ImageView)fragmentSMS.contactList.getChildAt(i).findViewById(R.id.contactCheckbox)).setImageResource(R.drawable.unchecked_checkbox_50);
                    }
                }
            }
        }
    }
}
