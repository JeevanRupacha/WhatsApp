package com.jeevan.whatsapp.Firestore;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Data.Message;

import java.util.List;
import java.util.Objects;


public class MyFirestore {


    //debug string
    private static final String TAG = "MyFirestore";

    /**
     * Firebase Firestore setup
     */
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    /**
     * Firebase Firestore
     */

    public MyFirestore() {
    }


    public void retrieveGroupMemberData(final String groupId, final Message message) {
        DocumentReference docRef = db.collection("Groups").document(groupId);

        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();

                            assert snapshot != null;
                            if (snapshot.exists()) {
                                List<String> members = (List<String>) Objects.requireNonNull(snapshot.getData()).get(FeedDataEntry.MEMBERS);

                                assert members != null;
                                for (String userId : members) {
                                    saveMessageToUsers(userId, groupId, message);
                                }
                            } else {
                                Log.d(TAG, "onComplete: No document exits");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Fail to get group Id document");
            }
        });
    }


    public void saveMessageToUsers(String userId, String groupId, Message message) {
        db.collection("Users").document(userId)
                .collection("MessageDocs")
                .document(groupId)
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
    }
}
