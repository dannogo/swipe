package com.sqisland.swipe;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
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
import java.util.Arrays;
import java.util.Random;

/**
 * Created by oleh on 11/25/15.
 */
public class SpeedDialAdapter extends RecyclerView.Adapter<SpeedDialAdapter.SpeedDialViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    protected ArrayList<String> names;
    protected ArrayList<String> phones;
    protected ArrayList<String> photos;
    int[] speedDialColors;
    protected ArrayList<Integer> colors;
    private FragmentSMS fragmentSMS;
    private SharedPreferences prefs;

    public SpeedDialAdapter(Context context, FragmentSMS fragmentSMS) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.fragmentSMS = fragmentSMS;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String[] phonesArray = (prefs.getString("phonesArray", "")).split(",");
        if (!phonesArray[0].equals("")) {
            names = new ArrayList<>(Arrays.asList((prefs.getString("namesArray", "")).split(",")));
            phones = new ArrayList<>(Arrays.asList(phonesArray));
            photos = new ArrayList<>(Arrays.asList((prefs.getString("photosArray", "")).split(",")));
            colors = new ArrayList<>();
            String[] colorsArray = (prefs.getString("colorsArray", "")).split(",");
            for (int i = 0; i < colorsArray.length; i++) {
                colors.add(Integer.parseInt(colorsArray[i]));
            }
        }else{
            names = new ArrayList<>();
            phones = new ArrayList<>();
            photos = new ArrayList<>();
            colors = new ArrayList<>();
        }
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

        if (ServingClass.checkedPhones.contains(phones.get(position))){
            holder.speedDialCheckmark.setImageResource(R.drawable.checked_checkbox_50_white);
        }else{
            holder.speedDialCheckmark.setImageResource(R.drawable.unchecked_checkbox_50_white);
        }

        if ((photos.get(position) != null) && !(photos.get(position).equals("null"))){
            holder.speedDialPhoto.setVisibility(View.VISIBLE);
            holder.speedDialFirstCharacter.setVisibility(View.INVISIBLE);
            holder.speedDialPhoto.setImageURI(Uri.parse(photos.get(position)));
        }else{
            holder.speedDialPhoto.setVisibility(View.GONE);
            holder.speedDialFirstCharacter.setVisibility(View.VISIBLE);
            holder.speedDialFirstCharacter.setText(names.get(position).substring(0, 1));
        }

        holder.speedDialRowContent.setBackgroundColor(colors.get(position));
        holder.speedDialName.setText(names.get(position));
        holder.speedDialPhone.setText(phones.get(position));
    }

    @Override
    public int getItemCount() {
        return phones.size();
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

                String phoneForDeletingFromSpeedDial = phones.get(getAdapterPosition());

                names.remove(getAdapterPosition());
                phones.remove(getAdapterPosition());
                photos.remove(getAdapterPosition());
                colors.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());

                ContactAdapter contactAdapter = (ContactAdapter) fragmentSMS.contactList.getAdapter();
                contactAdapter.staredPhones.remove(phoneForDeletingFromSpeedDial);

                ServingClass.saveStarsToSharedPreferences(context, contactAdapter.staredPhones);

                for (int i=0; i<fragmentSMS.contactList.getChildCount(); i++) {
                    if ((((TextView) fragmentSMS.contactList.getChildAt(i).findViewById(R.id.contactDescription)).getText().toString().equals(phoneForDeletingFromSpeedDial))){
                        ((ImageView)fragmentSMS.contactList.getChildAt(i).findViewById(R.id.contactStar)).setImageResource(R.drawable.empty_star);
                    }
                }

                ServingClass.handleCountChangeInSpeedDial(context, fragmentSMS.speedDial, false);
                if (phones.size() == 0){
                    fragmentSMS.expandSpeedDial.setVisibility(View.GONE);
                }

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
