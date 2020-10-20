package com.jeevan.whatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.RotatingCircle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    //local variables
    private String groupName , groupId;

    //Log debug
    private static final String TAG = "GroupActivity";

    //main page views variables


    //progress bar custom library spin-kit github
    private ProgressBar progressBar;


    /**
     * Firebase Firestore setup
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    /**
     * Firebase Firestore
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        initializeFields();
        setUpToolbar();

    }

    private void initializeFields(){

        groupName = getIntent().getStringExtra(FeedDataEntry.GROUP_TITLE);
        groupId = getIntent().getStringExtra(FeedDataEntry.GROUP_ID);
        Log.d(TAG, "setUpToolbar: " + groupName + " and group id " + groupId);
    }



    private void setUpToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.group_activity_app_bar);
        toolbar.setTitle(groupName);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(GroupActivity.this, MainActivity.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpSpinkKit()
    {
    //        Sprite rotatingCircle = new RotatingCircle();
    //        progressBar.setIndeterminateDrawable(rotatingCircle);
    }


    @Override
    public void onClick(View v) {

//        switch (v.getId())
//        {
//            case R.id.registerNewAccountTextView : sendToRegisterAccount();
//                break;
//            case R.id.loginButton : login();
//                break;
//            default:break;
//        }
    }


    private void updateUI(FirebaseUser user) {
        if(user != null)
        {
            sentToMainActivity();
        }
    }

    private void sentToMainActivity() {
        Intent intent = new Intent(GroupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
