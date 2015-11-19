package com.sqisland.swipe;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by oleh on 18-Nov-15.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{

    private LayoutInflater inflater;
    private ArrayList<String> ids;
    private ArrayList<String> names;
    private ArrayList<String> phones;
    private ArrayList<String> photos;
    protected ArrayList<String> checkedPhones = new ArrayList<>();
    protected ArrayList<String> staredPhones = new ArrayList<>();
    Context context;

    public ContactAdapter(Context context, ArrayList<String> ids,
                          ArrayList<String> names, ArrayList<String> phones, ArrayList<String> photos){
        inflater = LayoutInflater.from(context);
        this.ids = new ArrayList<>(ids);
        this.names = new ArrayList<>(names);
        this.phones = new ArrayList<>(phones);
        this.photos = new ArrayList<>(photos);
        this.context = context;

    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contact_row, parent, false);
        ContactViewHolder holder = new ContactViewHolder(view, context);
        return holder;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        if (photos.get(position) == null) {
            holder.icon.setImageResource(R.drawable.contact);
        }else{
            holder.icon.setImageURI(Uri.parse(photos.get(position)));
        }
        if (checkedPhones.contains(phones.get(position))){
            holder.checkbox.setImageResource(R.drawable.checked_checkbox_50);
        }else{
            holder.checkbox.setImageResource(R.drawable.unchecked_checkbox_50);
        }
        if (staredPhones.contains(phones.get(position))){
            holder.contactStar.setImageResource(R.drawable.star);
        }else{
            holder.contactStar.setImageResource(R.drawable.empty_star);
        }


        holder.name.setText(names.get(position));
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

        public ContactViewHolder(View itemView, Context context) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.contactIcon);
            name = (TextView) itemView.findViewById(R.id.contactName);
            phone = (TextView) itemView.findViewById(R.id.contactDescription);
            databaseID = (TextView) itemView.findViewById(R.id.databaseID);
            checkbox = (ImageView) itemView.findViewById(R.id.contactCheckbox);
            contactStar = (ImageView) itemView.findViewById(R.id.contactStar);

            checkbox.setOnClickListener(this);
            itemView.setOnClickListener(this);
            contactStar.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String currentPhone = phone.getText().toString();
            if ((view.getId() == contactStar.getId()) ){
                if (!staredPhones.contains(currentPhone)){
                    staredPhones.add(currentPhone);
                    contactStar.setImageResource(R.drawable.star);
                }else{
                    staredPhones.remove(currentPhone);
                    contactStar.setImageResource(R.drawable.empty_star);
                }
            }else{
                if (!checkedPhones.contains(currentPhone)) {
                    checkedPhones.add(currentPhone);
                    checkbox.setImageResource(R.drawable.checked_checkbox_50);
                }else{
                    checkedPhones.remove(currentPhone);
                    checkbox.setImageResource(R.drawable.unchecked_checkbox_50);
                }
            }
        }
    }
}
