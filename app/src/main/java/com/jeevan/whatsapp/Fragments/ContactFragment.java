package com.jeevan.whatsapp.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.BuildConfig;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;
import com.jeevan.whatsapp.Ui.RecyclerView.ContactListRecyclerViewAdapter;
import com.jeevan.whatsapp.Ui.RecyclerView.FriendListRecyclerViewAdapter;
import com.jeevan.whatsapp.WhatsAppMVVC.WhatsAppDataModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContactFragment extends Fragment
{
    public static final String TAG = "ContactFragment";

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    //Fields variables
    private RecyclerView recyclerView;
    private ContactListRecyclerViewAdapter mAdapter;



    private ArrayList<Map> mDataset;
    private static ContactFragment INSTANCE = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
    }


    @Override
    public void onStart() {
        super.onStart();
        loadContactsPermission();
    }

    public static ContactFragment getInstance()
    {
        if(INSTANCE == null){
            INSTANCE = new ContactFragment();
        }
        return INSTANCE;
    }

    private void initializeFields()
    {
        recyclerView = getView().findViewById(R.id.contact_recyclerView);
        mDataset = new ArrayList<>();

    }

    private void setUpRecyclerView() {

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        Log.d(TAG, "setUpRecyclerView: "+ mDataset);
        mAdapter = new ContactListRecyclerViewAdapter(mDataset, getContext());
        recyclerView.setAdapter(mAdapter);
    }

    public void loadContactsPermission()
    {

        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //no permission is allowed so have to ask
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {
                //inform user to allow access
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);

            }else {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            }

        } else {
            //already have permission no need to ask for permission
            new ContactLoadAsyncTask().execute();
        }

    }

    private void getContacts() {
        // Android version is lesser than 6.0 or the permission is already granted.

        ArrayList<Map<String, String>> contacts = new ArrayList<>();
        Map<String, String> contactMap;

        ContentResolver contentResolver = Objects.requireNonNull(getActivity()).getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.Contacts.DISPLAY_NAME);


        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    Cursor cursor1 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id}, null);

                    while (cursor1.moveToNext()) {
                        String phoneNumber = cursor1.getString(cursor1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactMap = new HashMap<>();
                        contactMap.put((String) FeedDataEntry.CONTACT_NAME, name);
                        contactMap.put((String) FeedDataEntry.CONTACT_NUMBER, phoneNumber);
                        contacts.add(contactMap);
                    }
                    cursor1.close();
                }
            }
        }
        cursor.close();

        //make empty dataset before adding any data into it
        mDataset.clear();
        mDataset.addAll(contacts);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_READ_CONTACTS)
        {

            if(grantResults.length > 0) {
                if( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: granted");
                    Toast.makeText(getActivity(), "Access Allowed", Toast.LENGTH_SHORT).show();
                    //Permission is granted
                    //only runs on at once if user allow permission

                }else{
                    Toast.makeText(getActivity(), "Access denied !", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public class ContactLoadAsyncTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            getContacts();
          return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setUpRecyclerView();
        }
    }



}

