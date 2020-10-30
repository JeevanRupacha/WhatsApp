package com.jeevan.whatsapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsProfileActivity extends AppCompatActivity {


    private static final String TAG = "FriendsProfileActivity";



    //Fields variables
    private TextView username, userBio;
    private CircleImageView profileImage;
    private Button messageButton;

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

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPrivateMessageActivity(hashMap);
            }
        });
    }

    private void sendToPrivateMessageActivity(Map map) {
        Intent intent = new Intent(this, PrivateMessageActivity.class);
        intent.putExtra("hashMap", (Serializable) map);
        this.startActivity(intent);
    }

    private void updateUI() {
        username.setText(hashMap.get("username"));
        userBio.setText(hashMap.get("profileBio"));
        if(hashMap.get(FeedDataEntry.PROFILE_IMAGE_CIRCLE_SOURCE) != null ){
            Picasso.get().load(hashMap.get(FeedDataEntry.PROFILE_IMAGE_CIRCLE_SOURCE)).into(profileImage);
        }
    }


    private void initializeFields()
    {
        username = findViewById(R.id.friends_profile_username);
        userBio = findViewById(R.id.friends_profile_bio);
        profileImage = findViewById(R.id.find_friends_profile_profileImage);
        messageButton = findViewById(R.id.message_friend_button);

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