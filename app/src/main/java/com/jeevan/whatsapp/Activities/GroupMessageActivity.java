package com.jeevan.whatsapp.Activities;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Data.Message;
import com.jeevan.whatsapp.Firestore.MyFirestore;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;

import org.w3c.dom.Document;

import java.util.ArrayList;
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
        retrieveMessage(groupId);
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


        switch (item.getItemId())
        {
            case android.R.id.home : goBackToHome();
                break;
            case R.id.leaveGroup: leaveGroup();
            break;
            case R.id.report: report();
            break;
            default:break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void leaveGroup(){
        Toast.makeText(this, "leave group", Toast.LENGTH_SHORT).show();

       removeFromGroups();
       removeFromUsers();

    }

    private void removeFromUsers() {
        /////update the arraylist of group id list in user document after remove group id
        DocumentReference groupListIdRef = db.collection("Users")
                .document(firebaseAuth.getCurrentUser().getUid());

        groupListIdRef.update("groupList", FieldValue.arrayRemove(groupId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sentToMainActivity();
                        Log.d(TAG, "onSuccess:  removed the group Id from user grouplist ");
                    }
                });
    }

    private void removeFromGroups() {
        /////remove the member from arraylist of group members list in Groups document
        DocumentReference groupListIdRef = db.collection("Groups")
                .document(groupId);

        groupListIdRef.update("members", FieldValue.arrayRemove(firebaseAuth.getCurrentUser().getUid()))
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: removed from groups");
            }
        });

    }



    private void report()
    {
        Toast.makeText(this, "report", Toast.LENGTH_SHORT).show();
    }

    private void goBackToHome()
    {
        startActivity(new Intent(GroupMessageActivity.this, MainActivity.class));
        finish(); // close this activity and return to preview activity (if there is any)
    }



    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.send_message_button_group : sendMessage(groupId);
                break;
            default:break;
        }
    }

    private void retrieveMessage(String messageId) {
        messageTextView.clearComposingText();
        DocumentReference docRef = db.collection("MessagesCollection")
                .document(firebaseAuth.getCurrentUser().getUid())
                .collection("MyMessages")
                .document(messageId);

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
                        //TODO fix auto scroll when message is not seen
                        scrollView.setSmoothScrollingEnabled(true);
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

    public void sendMessage(String messageId)
    {
       saveMessageInDocument(messageId);
       clearInputFields();
    }

    private void clearInputFields() {
        //clears all the input fields after the massage send
        inputMessage.setText("");
    }

    private void saveMessageInDocument(String messageId) {
        if(TextUtils.isEmpty(inputMessage.getText().toString().trim()))
        {
            return;
        }
        //Message object
        Message message = new Message();
        String msg = inputMessage.getText().toString().trim();
        message.setMessage(msg);
        message.setMessageType("text");
        message.setTimeAdded(System.currentTimeMillis());
        message.setMessageAdminId(firebaseAuth.getCurrentUser().getUid());

        /**
         * First retrieve the member in group document and
         * by looping them add all the message in users message with
         * group id
         * save message in message document inside users message docs
         */
            saveMessages(messageId, message);
        /**
         * close
         */

    }

    public void saveMessages(final String messageId, final Message message) {

        /**
         * First you have to loop all the group members
         * and assign message to all the group members
         * Here message id id group Id
         */

        db.collection("Groups")
                .document(messageId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot snapshot = task.getResult();
                            if(snapshot.exists())
                            {
                                ArrayList<String> membersId = (ArrayList<String>) snapshot.getData().get("members");

                                for (String memberId : membersId)
                                {
                                    saveMessageToMembers(memberId, messageId, message);
                                }

                            }
                        }else{
                            Log.d(TAG, "onComplete: fail to get task" );
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });


    }

    private void saveMessageToMembers(String userId, String messageId, Message message)
    {
        db.collection("MessagesCollection")
                .document(userId)
                .collection("MyMessages")
                .document(messageId)
                .collection("Messages")
                .add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "onSuccess: saved message in message document ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Failed to send message ");
            }
        });


        /**
         * Adding field in MessagesCollection fields
         * for to identify last message
         * This message will be last message send by any users
         */
        db.collection("MessagesCollection")
                .document(userId)
                .collection("MyMessages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Added last message ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to send message ");
                    }
                });
    }


    private void sentToMainActivity() {
        Intent intent = new Intent(GroupMessageActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
