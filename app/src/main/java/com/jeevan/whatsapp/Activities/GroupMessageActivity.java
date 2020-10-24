package com.jeevan.whatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Data.Message;
import com.jeevan.whatsapp.Firestore.MyFirestore;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;

import java.util.List;
import java.util.Objects;

public class GroupMessageActivity extends AppCompatActivity implements View.OnClickListener {

    //local variables
    private String groupName , groupId;

    //Log debug
    private static final String TAG = "GroupActivity";

    //Fields variables
    private EditText inputMessage;
    private ImageButton sendButton;
    private TextView messageTextView;
    private ScrollView scrollView;


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

        sendButton.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayMessageData();
    }

    private void displayMessageData() {
        retrieveMessage();
    }

    private void retrieveMessage() {
        DocumentReference docRef = db.collection("Users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("MessageDocs")
                .document(groupId);

        docRef.collection("Messages")
                .orderBy("timeAdded")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        for(DocumentChange dc: value.getDocumentChanges())
                        {
                            messageTextView.append("\n" +(CharSequence) dc.getDocument().getData().get(FeedDataEntry.MESSAGE) + " \n" +
                                    (CharSequence) dc.getDocument().getData().get(FeedDataEntry.MESSAGE_ADMIN_ID));

                            Log.d(TAG, "onEvent: " + dc.getDocument().getData());
                        }

                        /**
                         * https://stackoverflow.com/questions/5101448/android-auto-scrolling-down-the-edittextview-for-chat-apps
                         * concept with stackoverflow
                         * Auto scroll when message is changed
                         */
                        scrollView.post(new Runnable() {
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                        /**
                         * close
                         */
                    }
                });
    }

    private void initializeFields(){
        inputMessage = findViewById(R.id.input_message_editText_group);
        sendButton = findViewById(R.id.send_message_button_group);
        messageTextView = findViewById(R.id.messageTextView);
        scrollView = findViewById(R.id.group_message_scrollView);

        //getting the data passed through the intent activity
        groupName = getIntent().getStringExtra(FeedDataEntry.GROUP_TITLE);
        groupId = getIntent().getStringExtra(FeedDataEntry.GROUP_ID);
        Log.d(TAG, "setUpToolbar: " + groupName + " and group id " + groupId);
    }



    private void setUpToolbar() {

        Toolbar toolbar =  findViewById(R.id.group_activity_app_bar);
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
            startActivity(new Intent(GroupMessageActivity.this, MainActivity.class));
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

        switch (v.getId())
        {
            case R.id.send_message_button_group : sendMessage();
                break;
            default:break;
        }
    }


    private void updateUI(FirebaseUser user) {
        if(user != null)
        {
            sentToMainActivity();
        }
    }

    private void sendMessage()
    {
       saveMessageInDocument();
       clearInputFields();
    }

    private void clearInputFields() {
        //clears all the input fields after the massage send
        inputMessage.setText("");
    }

    private void saveMessageInDocument() {
        if(TextUtils.isEmpty(inputMessage.getText().toString().trim()))
        {
            return;
        }
        //Message object
        Message message = new Message();
        String msg = inputMessage.getText().toString().trim();
        message.setMessage(msg);
        message.setTimeAdded(System.currentTimeMillis());
        message.setMessageAdminId(firebaseAuth.getCurrentUser().getUid());

        /**
         * First retrieve the member in group document and
         * by looping them add all the message in users message with
         * group id
         * save message in message document inside users message docs
         */
        MyFirestore myFirestore = new MyFirestore();
        myFirestore.retrieveGroupMemberData(groupId, message);
        /**
         * close
         */

    }


    private void sentToMainActivity() {
        Intent intent = new Intent(GroupMessageActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
