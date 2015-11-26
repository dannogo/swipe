package com.sqisland.swipe;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by oleh on 18-Nov-15.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{

    private LayoutInflater inflater;
    protected ArrayList<String> ids;
    protected ArrayList<String> names;
    protected ArrayList<String> phones;
    protected ArrayList<String> photos;
    protected ArrayList<String> staredPhones = new ArrayList<>();
    private RecyclerView speedDial;
    private String searchSubstring;
    private Context context;

    public ContactAdapter(Context context, ArrayList<String> ids,
                          ArrayList<String> names, ArrayList<String> phones, ArrayList<String> photos,
                          String searchSubstring, RecyclerView speedDial){
        inflater = LayoutInflater.from(context);
        if (ServingClass.temporaryPhones.isEmpty() || (searchSubstring != null)) {
            this.ids = new ArrayList<>(ids);
            this.names = new ArrayList<>(names);
            this.phones = new ArrayList<>(phones);
            this.photos = new ArrayList<>(photos);
        }else{
            this.ids = new ArrayList<>(ServingClass.temporaryPhonesIds);
            this.names = new ArrayList<>();
            this.phones = new ArrayList<>(ServingClass.temporaryPhones);
            this.photos = new ArrayList<>();
            for (int i=0; i<ServingClass.temporaryPhones.size(); i++){
                this.names.add(context.getResources().getString(R.string.temporary));
                this.photos.add(context.getResources().getString(R.string.temporary));
            }
            this.ids.addAll(ids);
            this.names.addAll(names);
            this.phones.addAll(phones);
            this.photos.addAll(photos);
        }

        this.searchSubstring = searchSubstring;
        this.context = context;
        this.speedDial = speedDial;

    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contact_row, parent, false);
        ContactViewHolder holder = new ContactViewHolder(view, context);
        return holder;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if (ids.get(position).startsWith(context.getResources().getString(R.string.temporary))){
            holder.icon.setImageResource(R.drawable.trailer);
            holder.contactStar.setVisibility(View.GONE);
            holder.deleteTemporary.setVisibility(View.VISIBLE);
        }else {
            holder.contactStar.setVisibility(View.VISIBLE);
            holder.deleteTemporary.setVisibility(View.GONE);
            if (photos.get(position) == null) {
                holder.icon.setImageResource(R.drawable.contact);
            } else {
                holder.icon.setImageURI(Uri.parse(photos.get(position)));
            }

            if (staredPhones.contains(phones.get(position))){
                holder.contactStar.setImageResource(R.drawable.star);
            }else{
                holder.contactStar.setImageResource(R.drawable.empty_star);
            }
        }
        if (ServingClass.checkedPhones.contains(phones.get(position))){
            holder.checkbox.setImageResource(R.drawable.checked_checkbox_50);
        }else{
            holder.checkbox.setImageResource(R.drawable.unchecked_checkbox_50);
        }


        if (searchSubstring != null){
            int substringStartIndex = names.get(position).toLowerCase().indexOf(searchSubstring);
            int subStringEndIndex = substringStartIndex + searchSubstring.length();
            Spannable WordtoSpan = new SpannableString(names.get(position));
//            WordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#e74c3c")), substringStartIndex, subStringEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            WordtoSpan.setSpan(new BackgroundColorSpan(Color.parseColor("#FFECB3")), substringStartIndex, subStringEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(WordtoSpan);
        }else{
            holder.name.setText(names.get(position));
        }
//        holder.name.setText(Html.fromHtml(names.get(position)));
        holder.phone.setText(phones.get(position));
        holder.databaseID.setText(ids.get(position));
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ImageView icon;
        private TextView name;
        private TextView phone;
        private TextView databaseID;
        private ImageView checkbox;
        private ImageView contactStar;
        private ImageView deleteTemporary;
        private SpeedDialAdapter speedDialAdapter;

        public ContactViewHolder(View itemView, Context context) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.contactIcon);
            name = (TextView) itemView.findViewById(R.id.contactName);
            phone = (TextView) itemView.findViewById(R.id.contactDescription);
            databaseID = (TextView) itemView.findViewById(R.id.databaseID);
            checkbox = (ImageView) itemView.findViewById(R.id.contactCheckbox);
            contactStar = (ImageView) itemView.findViewById(R.id.contactStar);
            deleteTemporary = (ImageView) itemView.findViewById(R.id.deleteTemporary);

            speedDialAdapter = (SpeedDialAdapter)speedDial.getAdapter();


            checkbox.setOnClickListener(this);
            itemView.setOnClickListener(this);
            contactStar.setOnClickListener(this);
            deleteTemporary.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String currentPhone = phone.getText().toString();
            if ((view.getId() == contactStar.getId()) ){
                int currentPosition = phones.indexOf(currentPhone);
                if (!staredPhones.contains(currentPhone)){
                    staredPhones.add(currentPhone);
                    contactStar.setImageResource(R.drawable.star);

                    speedDialAdapter.ids.add(ids.get(currentPosition));
                    speedDialAdapter.names.add(names.get(currentPosition));
                    speedDialAdapter.phones.add(currentPhone);
                    speedDialAdapter.photos.add(photos.get(currentPosition));
                    speedDialAdapter.notifyItemInserted(speedDialAdapter.getItemCount() - 1);

                    // It`s a spike for nullifying some tricky bug
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            speedDial.scrollToPosition(speedDialAdapter.getItemCount() - 1);
                        }
                    }, 50);

                }else{
                    staredPhones.remove(currentPhone);
                    contactStar.setImageResource(R.drawable.empty_star);
                }
            }else if((view.getId() == deleteTemporary.getId())) {
                Toast.makeText(context, "Temporary phone number deleted", Toast.LENGTH_SHORT).show();
                for (int i=0; i<ids.size(); i++){
                    if (ids.get(i).equals(databaseID.getText().toString())){
                        ids.remove(i);
                        names.remove(i);
                        phones.remove(i);
                        photos.remove(i);
                        notifyItemRemoved(i);
                        ServingClass.temporaryPhonesIds.remove(databaseID.getText().toString());
                        ServingClass.temporaryPhones.remove(phone.getText().toString());
                        break;
                    }
                }
                if (ServingClass.checkedPhones.contains(phone.getText().toString())){
                    ServingClass.checkedPhones.remove(phone.getText().toString());
                }
            }else{
                if (!ServingClass.checkedPhones.contains(currentPhone)) {
                    ServingClass.checkedPhones.add(currentPhone);
                    checkbox.setImageResource(R.drawable.checked_checkbox_50);
                }else{
                    ServingClass.checkedPhones.remove(currentPhone);
                    checkbox.setImageResource(R.drawable.unchecked_checkbox_50);
                }

                // synchronizing checkmarks with speed dial list
                for (int i=0; i<speedDial.getChildCount(); i++){
                    if (ServingClass.checkedPhones.contains(((TextView) speedDial.getChildAt(i).findViewById(R.id.speedDialPhone)).getText().toString())) {
                        ((ImageView) speedDial.getChildAt(i).findViewById(R.id.speedDialCheckmark)).setImageResource(R.drawable.checked_checkbox_50_white);
                    }else{
                        ((ImageView)speedDial.getChildAt(i).findViewById(R.id.speedDialCheckmark)).setImageResource(R.drawable.unchecked_checkbox_50_white);
                    }
                }

            }
        }
    }
}
