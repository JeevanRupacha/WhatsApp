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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PrivateMessageActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Local variables
     * the id here is not my own id but id to who send message
     */

    private String receiverName , receiverId;

    //Log debug
    private static final String TAG = "PrivateMessageActivity";

    //Fields variables
    private EditText inputMessage;
    private ImageButton sendButton;
    private TextView messageTextView;
    private ScrollView scrollView;

    private HashMap<String, String> hashMap;


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
        setContentView(R.layout.activity_private_message);

        initializeFields();
        setUpToolbar();

        sendButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveMessage(receiverId);

    }


    private void initializeFields(){
        inputMessage = findViewById(R.id.input_message_editText_group);
        sendButton = findViewById(R.id.send_message_button_group);
        messageTextView = findViewById(R.id.messageTextView);
        scrollView = findViewById(R.id.group_message_scrollView);

        Intent intent = getIntent();
        hashMap = (HashMap<String, String>)intent.getSerializableExtra("hashMap");

        //getting the data passed through the intent activity
        receiverName = hashMap.get("username");
        receiverId = hashMap.get("userID");
    }



    private void setUpToolbar() {

        Toolbar toolbar =  findViewById(R.id.group_activity_app_bar);
        toolbar.setTitle(receiverName);
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
            case R.id.report: report();
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
    }




    private void report()
    {
        Toast.makeText(this, "report", Toast.LENGTH_SHORT).show();
    }

    private void goBackToHome()
    {
        startActivity(new Intent(PrivateMessageActivity.this, MainActivity.class));
        finish(); // close this activity and return to preview activity (if there is any)
    }


    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.send_message_button_group :sendMessage(receiverId);
                break;
            default:break;
        }
    }


    private void retrieveMessage(String messageId) {

        /**
         * Here messageId is receiverId which is for identify message
         */

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
         * Save message in both users
         * To senders and
         * To receivers
         */
        saveMessages(messageId, message);
        /**
         * close
         */

    }

    public void saveMessages(String messageId, Message message) {
        //here messageId is receiver's id
        saveMessage(messageId,firebaseAuth.getCurrentUser().getUid(), message);
        saveMessage(firebaseAuth.getCurrentUser().getUid(),messageId, message);
    }

    private void saveMessage(String receiverId, String senderId, Message message) {
        db.collection("MessagesCollection")
                .document(receiverId)
                .collection("MyMessages")
                .document(senderId)
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
                .document(receiverId)
                .collection("MyMessages")
                .document(senderId)
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

}
