package com.sqisland.swipe;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by oleh on 13-Nov-15.
 */
public class FragmentSMS extends Fragment {

    protected RecyclerView contactList, speedDial ;
    private ContactAdapter contactAdapter;
    private SpeedDialAdapter speedDialAdapter;
    private RelativeLayout progressBar;
    private ArrayList<String> ids = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> phones = new ArrayList<>();
    private ArrayList<String> photos = new ArrayList<>();
    android.support.design.widget.FloatingActionButton fab;
    android.support.design.widget.FloatingActionButton fabTypeNumber;
    protected EditText searchField;
    ImageView expandSpeedDial;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sms, container, false);


        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        expandSpeedDial = (ImageView) rootView.findViewById(R.id.expandSpeedDial);
        expandSpeedDial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expand = prefs.getBoolean("expand", true);

                ServingClass.handleExpandCollapseSpeedDial(getActivity(), FragmentSMS.this, !expand);
            }
        });
        speedDial = (RecyclerView) rootView.findViewById(R.id.speedDial);
        speedDial.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.number_of_columns_in_speeddial)));
        speedDialAdapter = new SpeedDialAdapter(getActivity(), this);
        speedDial.setAdapter(speedDialAdapter);

        fab = (android.support.design.widget.FloatingActionButton) rootView.findViewById(R.id.fabSMS);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), ""+ServingClass.checkedPhones, Toast.LENGTH_SHORT).show();
                for (int i=0; i<contactList.getChildCount(); i++){
                    ((ImageView)contactList.getChildAt(i).findViewById(R.id.contactCheckbox)).setImageResource(R.drawable.unchecked_checkbox_50);
                }

                SmsManager sms = SmsManager.getDefault();
                PendingIntent sentPI;
                String SENT = "SMS_SENT";
                sentPI = PendingIntent.getBroadcast(getActivity(), 0,new Intent(SENT), 0);

                for (int i=0; i<ServingClass.checkedPhones.size(); i++){
                    sms.sendTextMessage(ServingClass.checkedPhones.get(i).replaceAll("[^+0-9]",""), null, getResources().getString(R.string.share_message), sentPI, null);
                }

                ServingClass.checkedPhones = new ArrayList<String>();

            }
        });

        final EditText numberEditField = (EditText) rootView.findViewById(R.id.numberEditField);
        final RelativeLayout fragmentSmsContent = (RelativeLayout) rootView.findViewById(R.id.fragmentSmsContent);
        final android.support.design.widget.TextInputLayout validatingLayout = (TextInputLayout) rootView.findViewById(R.id.validatingLayout);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);

        numberEditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if (numberEditField.getText().toString().matches("^[+]?[0-9]{10,13}$")){

                        contactAdapter.ids.add(0, getResources().getString(R.string.temporary)+ServingClass.temporaryPhonesCounter);
                        contactAdapter.names.add(0, getResources().getString(R.string.temporary));
                        contactAdapter.phones.add(0, v.getText().toString());
                        contactAdapter.photos.add(0, getResources().getString(R.string.temporary));

                        ServingClass.temporaryPhones.add(0, v.getText().toString());
                        ServingClass.temporaryPhonesIds.add(0, getResources().getString(R.string.temporary) + ServingClass.temporaryPhonesCounter++);

                        contactAdapter.notifyItemInserted(0);
                        contactList.scrollToPosition(0);

                        validatingLayout.setVisibility(View.GONE);
                        numberEditField.setText("");
                        fragmentSmsContent.setVisibility(View.VISIBLE);
                        validatingLayout.setErrorEnabled(false);
                        ViewCompat.setBackgroundTintList(numberEditField, new ColorStateList(new int[][]{new int[]{0xFFde3309}}, new int[]{Color.parseColor("#00BFA5")}));
                        validatingLayout.setHint(getResources().getString(R.string.type_number));
                    }else{
                        validatingLayout.setError("Invalide Number");
                        validatingLayout.setHint("Try again");
                        return true;
                    }
                }
                imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
                return false;
            }
        });

        fabTypeNumber = (FloatingActionButton) rootView.findViewById(R.id.fabTypeNumber);
        fabTypeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentSmsContent.setVisibility(View.GONE);
                validatingLayout.setVisibility(View.VISIBLE);
                numberEditField.requestFocus();
                imm.showSoftInput(numberEditField, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        progressBar = (RelativeLayout) rootView.findViewById(R.id.progressBar);
        contactList = (RecyclerView) rootView.findViewById(R.id.contactList);
        contactList.addItemDecoration(new DividerItemDecoration(getActivity(), null, true, true));

        final Handler handler = new Handler();
        final ArrayList<String> searchResultIds = new ArrayList<>();
        final ArrayList<String> searchResultNames = new ArrayList<>();
        final ArrayList<String> searchResultPhones = new ArrayList<>();
        final ArrayList<String> searchResultPhotos = new ArrayList<>();


        searchField = (EditText) rootView.findViewById(R.id.search_field);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeMessages(0);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchResultIds.clear();
                        searchResultNames.clear();
                        searchResultPhones.clear();
                        searchResultPhotos.clear();

                        String lowerSubstring = searchField.getText().toString().toLowerCase();

                        if (!searchField.getText().toString().equals("")) {
                            for (int i = 0; i < names.size(); i++) {
                                if (names.get(i).toLowerCase().contains(lowerSubstring)) {
                                    searchResultIds.add(ids.get(i));
                                    searchResultNames.add(names.get(i));
                                    searchResultPhones.add(phones.get(i));
                                    searchResultPhotos.add(photos.get(i));
                                }
                            }
                            contactAdapter = new ContactAdapter(getActivity(), searchResultIds,
                                    searchResultNames, searchResultPhones, searchResultPhotos, searchField.getText().toString().toLowerCase(), speedDial, FragmentSMS.this);
                            contactList.setAdapter(contactAdapter);
                        }else{
                            contactAdapter = new ContactAdapter(getActivity(), ids,
                                    names, phones, photos, null, speedDial, FragmentSMS.this);
                            contactList.setAdapter(contactAdapter);
                        }
                    }
                }, 1000);


            }
        });
        new LoadContactData().execute();

        contactList.setLayoutManager(new LinearLayoutManager(getActivity()));

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int)offsetPx);
        contactList.addItemDecoration(bottomOffsetDecoration);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
        RelativeLayout.LayoutParams paramsTypeNumber = (RelativeLayout.LayoutParams) fabTypeNumber.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            params.bottomMargin = (int)getResources().getDimension(R.dimen.fab_plus_navbar_margin);
            paramsTypeNumber.bottomMargin = (int)getResources().getDimension(R.dimen.fab_plus_navbar_margin);
        }else{
            params.bottomMargin = (int)getResources().getDimension(R.dimen.fab_margin);
            paramsTypeNumber.bottomMargin = (int)getResources().getDimension(R.dimen.fab_margin);
        }
        fab.setLayoutParams(params);
        fabTypeNumber.setLayoutParams(paramsTypeNumber);

        return rootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
        RelativeLayout.LayoutParams paramsTypeNumber = (RelativeLayout.LayoutParams) fabTypeNumber.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.bottomMargin = (int)getResources().getDimension(R.dimen.fab_margin);
            paramsTypeNumber.bottomMargin = (int)getResources().getDimension(R.dimen.fab_margin);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            params.bottomMargin = (int)getResources().getDimension(R.dimen.fab_plus_navbar_margin);
            paramsTypeNumber.bottomMargin = (int)getResources().getDimension(R.dimen.fab_plus_navbar_margin);
        }
        fab.setLayoutParams(params);
        fabTypeNumber.setLayoutParams(paramsTypeNumber);
    }

    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
        private int mBottomOffset;

        public BottomOffsetDecoration(int bottomOffset) {
            mBottomOffset = bottomOffset;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int dataSize = state.getItemCount();
            int position = parent.getChildAdapterPosition(view);
            if (dataSize > 0 && position == dataSize - 1) {
                outRect.set(0, 0, 0, mBottomOffset);
            } else {
                outRect.set(0, 0, 0, 0);
            }

        }
    }

    class LoadContactData extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            ContentResolver cr = getActivity().getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String photoUri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                    if (Integer.parseInt(cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {


                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                new String[]{id},
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            // Skiping short and other inappropriate numbers
                            if (phoneNo.replaceAll("[^+0-9]","").matches("^[+]?[0-9]{10,13}$")) {

                                if (phones.isEmpty()) {
                                    ids.add(id);
                                    names.add(name);
                                    phones.add(phoneNo);
                                    photos.add(photoUri);
                                } else if (!(PhoneNumberUtils.compare(phones.get(phones.size() - 1), phoneNo))) {
                                    ids.add(id);
                                    names.add(name);
                                    phones.add(phoneNo);
                                    photos.add(photoUri);
                                }
                            }
                        }
                        pCur.close();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            contactAdapter = new ContactAdapter(getActivity(), ids, names, phones, photos, null, speedDial, FragmentSMS.this);
            contactList.setAdapter(contactAdapter);
            ServingClass.handleCountChangeInSpeedDial(getContext(), speedDial, true);
            progressBar.setVisibility(View.GONE);
            searchField.setVisibility(View.VISIBLE);

            boolean expand = prefs.getBoolean("expand", true);

            if (!expand){
                ServingClass.handleExpandCollapseSpeedDial(getActivity(), FragmentSMS.this, false);
            }else{
                ServingClass.handleExpandCollapseSpeedDial(getActivity(), FragmentSMS.this, true);
            }

            if (!(speedDialAdapter.phones.size() == 0)) {
                expandSpeedDial.setVisibility(View.VISIBLE);
            }
        }

    }
}


