package com.sqisland.swipe;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
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
    protected ArrayList<String> checkedPhones = new ArrayList<>();
    protected ArrayList<String> staredPhones = new ArrayList<>();
//    protected static ArrayList<String> temporaryPhones = new ArrayList<>();
    private Context context;

    public ContactAdapter(Context context, ArrayList<String> ids,
                          ArrayList<String> names, ArrayList<String> phones, ArrayList<String> photos){
        inflater = LayoutInflater.from(context);
        if (ServingClass.temporaryPhones.isEmpty()) {
            this.ids = new ArrayList<>(ids);
            this.names = new ArrayList<>(names);
            this.phones = new ArrayList<>(phones);
            this.photos = new ArrayList<>(photos);
        }else{
            this.ids = new ArrayList<>(ServingClass.temporaryPhonesCounter);
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
        if (checkedPhones.contains(phones.get(position))){
            holder.checkbox.setImageResource(R.drawable.checked_checkbox_50);
        }else{
            holder.checkbox.setImageResource(R.drawable.unchecked_checkbox_50);
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
        private ImageView deleteTemporary;

        public ContactViewHolder(View itemView, Context context) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.contactIcon);
            name = (TextView) itemView.findViewById(R.id.contactName);
            phone = (TextView) itemView.findViewById(R.id.contactDescription);
            databaseID = (TextView) itemView.findViewById(R.id.databaseID);
            checkbox = (ImageView) itemView.findViewById(R.id.contactCheckbox);
            contactStar = (ImageView) itemView.findViewById(R.id.contactStar);
            deleteTemporary = (ImageView) itemView.findViewById(R.id.deleteTemporary);

            checkbox.setOnClickListener(this);
            itemView.setOnClickListener(this);
            contactStar.setOnClickListener(this);
            deleteTemporary.setOnClickListener(this);
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
            }else if((view.getId() == deleteTemporary.getId())) {
                Toast.makeText(context, "Temporary phone number deleted", Toast.LENGTH_SHORT).show();
                for (int i=0; i<ids.size(); i++){
                    if (ids.get(i).equals(databaseID.getText().toString())){
                        ids.remove(i);
                        names.remove(i);
                        phones.remove(i);
                        photos.remove(i);
                        notifyItemRemoved(i);
                    }
                }
                if (checkedPhones.contains(phone.getText().toString())){
                    checkedPhones.remove(phone.getText().toString());
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
