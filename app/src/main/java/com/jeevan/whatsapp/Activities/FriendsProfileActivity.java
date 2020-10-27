package com.jeevan.whatsapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsProfileActivity extends AppCompatActivity {


    private static final String TAG = "FriendsProfileActivity";



    //Fields variables
    private TextView username, userBio;
    private CircleImageView profileImage;
    private Button requestButton;

    //intent passes value in form map
    private HashMap<String, String> hashMap;


    //firebase setup
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);


        initializeFields();
        setUpToolbar();
        retrieveUsersData();
        updateUI();

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void updateUI() {
        username.setText(hashMap.get("username"));
        userBio.setText(hashMap.get("profileBio"));
    }


    private void initializeFields()
    {
        username = findViewById(R.id.friends_profile_username);
        userBio = findViewById(R.id.friends_profile_bio);
        profileImage = findViewById(R.id.user_image_find_friends_list);
        requestButton = findViewById(R.id.request_friend_button);

        Intent intent = getIntent();
       hashMap = (HashMap<String, String>)intent.getSerializableExtra("hashMap");
    }



    public void retrieveUsersData()
    {

    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.friends_profile_toolbar);
        toolbar.setTitle(hashMap.get("username"));
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
            startActivity(new Intent(FriendsProfileActivity.this, FindFriendsActivity.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}