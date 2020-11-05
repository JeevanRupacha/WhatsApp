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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
import com.jeevan.whatsapp.Ui.RecyclerView.PrivateMessageListRecyclerViewAdapter;
import com.jeevan.whatsapp.WhatsAppMVVC.WhatsAppDataModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class PrivateMessageActivity extends AppCompatActivity implements View.OnClickListener {

    private static PrivateMessageActivity INSTANCE = null;

    /**
     * Local variables
     * the id here is not my own id but id to who send message
     */

    private static String receiverName , receiverId;
    private String receivedMessageProfileImage;
    private boolean retrievedMessageStatus = false;

    //Log debug
    private static final String TAG = "PrivateMessageActivity";

    //Fields variables
    private ImageButton sendButton;
    private ImageButton emojiToggleButton, photoInputButton;
    private EmojIconActions emojIconActions;
    private EmojiconEditText inputMessage;
    private ConstraintLayout rootView;

    private HashMap<String, String> hashMap;


    /**
     * Firebase Firestore setup
     */
    private  FirebaseFirestore db = FirebaseFirestore.getInstance();
    private  FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    /**
     * Firebase Firestore
     */

    //Fields variables
    private RecyclerView recyclerView;
    private PrivateMessageListRecyclerViewAdapter mAdapter;


    public static PrivateMessageActivity getInstance()
    {
        if(INSTANCE == null)
        {
            INSTANCE = new PrivateMessageActivity();
        }

        return INSTANCE;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_message);

        initializeFields();

        setUpToolbar();


        sendButton.setOnClickListener(this);
        emojiToggleButton.setOnClickListener(this);
        photoInputButton.setOnClickListener(this);

        emojIconActions = new EmojIconActions(this,rootView,inputMessage,emojiToggleButton);
        emojIconActions.ShowEmojIcon();

    }

    @Override
    protected void onStart() {
        super.onStart();
        new AsyncTaskBack().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(retrievedMessageStatus)
        {
            deleteMessageAfterSeen();
        }
    }

    private void initializeFields(){
        inputMessage = findViewById(R.id.emojiEditText);
        sendButton = findViewById(R.id.send_message_button_group);
        recyclerView = findViewById(R.id.private_message_recyclerView);
        emojiToggleButton = findViewById(R.id.emoji_toggle_button);
        photoInputButton = findViewById(R.id.image_button_input);
        rootView = findViewById(R.id.private_message_rootView);

        Intent intent = getIntent();
        hashMap = (HashMap<String, String>)intent.getSerializableExtra("hashMap");

        //getting the data passed through the intent activity
        receiverName = hashMap.get("username");
        receiverId = hashMap.get("userID");
        receivedMessageProfileImage = hashMap.get("profileImageSrc");

    }

    private void setUpRecyclerView(ArrayList<Map> mDataset) {

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        Log.d(TAG, "setUpRecyclerView: "+ mDataset);
        mAdapter = new PrivateMessageListRecyclerViewAdapter(mDataset, receivedMessageProfileImage,this);
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }



    private void setUpToolbar() {
        Toolbar toolbar =  findViewById(R.id.private_chat_app_bar);
        TextView username = toolbar.findViewById(R.id.username_app_bar);
        TextView phoneNumber = toolbar.findViewById(R.id.phoneNumber_app_bar);
        CircleImageView profileImage = toolbar.findViewById(R.id.user_profile_image_app_bar);

        if(getIntent().getStringExtra("phoneNumber") != null)
        {
            username.setText(getIntent().getStringExtra("contactName"));
            phoneNumber.setText(getIntent().getStringExtra("phoneNumber"));
        }else{
            username.setText(receiverName);
        }

        Picasso.get().load(receivedMessageProfileImage).placeholder(R.drawable.profile).into(profileImage);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId())
        {
            case android.R.id.home : goBackToHome();
                break;
            default:break;
        }

        return super.onOptionsItemSelected(item);
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
            case R.id.image_button_input : imageButtonToggle();
            break;
            default:break;
        }
    }

    private void imageButtonToggle() {

    }


    private void retrieveMessage() {

        String messageId = receiverId;
        /**
         * Here messageId is receiverId which is for identify message
         */

        final ArrayList<Map> mDataset = new ArrayList<>();

        mDataset.clear();
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
                            HashMap<String, Object> data = new HashMap<>();
                            data.putAll(dc.getDocument().getData());
                            data.put("messageKey",dc.getDocument().getId());
                            data.put("receiverId", receiverId);

                            if( !TextUtils.isEmpty(dc.getDocument().getId()))
                            {
                                mDataset.add(data);
                                Log.d(TAG, "onEvent: e " + dc.getDocument().getData());
                            }
                        }

                        Log.d(TAG, "onEvent: a "+ mDataset);
                        setUpRecyclerView(mDataset);
                        retrievedMessageStatus = true;


                        /**
                         * https://stackoverflow.com/questions/5101448/android-auto-scrolling-down-the-edittextview-for-chat-apps
                         * concept with stackoverflow
                         * Auto scroll when message is changed
                         */
                        //TODO fix auto scroll when message is not seen

                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                // Call smooth scroll
                                if(mAdapter.getItemCount() >0)
                                {
                                recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                                }
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
        inputMessage.clearFocus();
        inputMessage.requestFocus();
        inputMessage.setCursorVisible(true);
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
        message.setMessageAdminId(firebaseAuth.getCurrentUser().getUid());

        Timestamp timestamp = Timestamp.now();
        Long millis = timestamp.getSeconds()*1000;

        message.setTimeAdded(millis);

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

        String messageUniqueID = UUID.randomUUID().toString().replaceAll("-","");
        /**  here messageId is receiver's id
         * Save the message in both user
         * first to user who send the message
         * and save to whom the message is sent
         * Remember to delete message you have to go both user to detele for every one
         *
         */
        saveMessage(messageId,firebaseAuth.getCurrentUser().getUid(), message, messageUniqueID);
        saveMessage(firebaseAuth.getCurrentUser().getUid(),messageId, message,messageUniqueID);
    }

    private void saveMessage(String receiverId, String senderId, Message message,String messageUniqueID) {
        db.collection("MessagesCollection")
                .document(receiverId)
                .collection("MyMessages")
                .document(senderId)
                .collection("Messages")
                .document(messageUniqueID)
                .set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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

    private void deleteMessageAfterSeen()
    {
        DocumentReference docRef = db.collection("MessagesCollection")
                .document(receiverId)
                .collection("MyMessages")
                .document(firebaseAuth.getCurrentUser().getUid());

        docRef.collection("Messages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(DocumentSnapshot snapshot : task.getResult())
                            {
                                String userId = (String) snapshot.getData().get("messageAdminId");
                                if(!userId.equals(firebaseAuth.getCurrentUser().getUid()))
                                {
                                  deleteMessage(snapshot.getId());
                                }
                            }
                        }else{
                            Log.d(TAG, "onComplete: fail to get data");
                        }
                    }
                });
    }

    private void deleteMessage(String messageUniqueId)
    {
        new MyFirestore(receiverId, messageUniqueId).deleteMessage();
    }

    private class AsyncTaskBack extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
        retrieveMessage();
        return null;
    }

    }
}
