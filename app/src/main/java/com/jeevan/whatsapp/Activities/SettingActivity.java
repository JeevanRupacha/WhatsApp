package com.jeevan.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Data.UserProfile;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";


    //Fields variables
    private CircleImageView circleProfileImage;
    private EditText username, profileBio;
    private Button updateButton;

    //firebase setup
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setUpToolbar();
        initializeFields();
        updateUI();

        updateButton.setOnClickListener(this);
    }

    private void updateUI() {
        //updates the editable fields like user name user bio etc
        updateFields();
    }

    private void updateFields()
    {

        DocumentReference docRef = db.collection("Users")
                .document(auth.getCurrentUser().getUid());


                docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                //update Fields
                                String usernameText = String.valueOf(document.getData().get(FeedDataEntry.USERNAME));
                                String profileBioTExt = String.valueOf(document.getData().get(FeedDataEntry.PROFILE_BIO));
                                username.setText(usernameText);
                                profileBio.setText(profileBioTExt);
                                //TODO not working circleProfileImage.setImageResource(Integer.parseInt(FeedDataEntry.PROFILE_IMAGE_CIRCLE_SOURCE));

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }



    private void initializeFields()
    {
        circleProfileImage = findViewById(R.id.profile_image_settings);
        username = findViewById(R.id.edit_username);
        profileBio = findViewById(R.id.edit_profile_bio);
        updateButton = findViewById(R.id.settings_update_button);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        toolbar.setTitle(R.string.setting_titles);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(SettingActivity.this, MainActivity.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.settings_update_button : updateUserProfile();
            break;
            default:break;
        }
    }

    private void updateUserProfile() {
        UserProfile userProfile = new UserProfile();

        String usernameMap = username.getText().toString();
        String profileBioMap = profileBio.getText().toString();


        userProfile.setUserID(auth.getCurrentUser().getUid());
        userProfile.setUsername(usernameMap);
        userProfile.setProfileBio(profileBioMap);

        db.collection("Users")
                .document(auth.getCurrentUser().getUid())
                .set(userProfile, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: Success to add document");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Fail to update user data in Users collections error= " +e);
                    }
                });
    }


}