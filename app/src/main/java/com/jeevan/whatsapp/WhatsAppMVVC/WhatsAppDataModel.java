package com.jeevan.whatsapp.WhatsAppMVVC;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

public class WhatsAppDataModel extends ViewModel {

    //Debug log string
    private static final String TAG = WhatsAppDataModel.class.getSimpleName();

    //firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    //users data for recycler view of friend list
    private MutableLiveData<ArrayList<Map>> allUsersDocument = new MutableLiveData<>();;
    private MutableLiveData<Map> singleUserDocument = new MutableLiveData<>();;

    public LiveData<ArrayList<Map>> getUsersDocument() {
        loadUsersDocument();
        return allUsersDocument;
    }

    public LiveData<Map> getSingleUserDocument(String userId) {
        loadSingleUserDocument(userId);
        return singleUserDocument;
    }

    private void loadSingleUserDocument(String userId) {
        final DocumentReference docRef = db.collection("Users")
                .document(userId);


        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null)
                {
                    Log.d(TAG, "onEvent: Error occurs "+ error);
                    return;
                }
                singleUserDocument.postValue(value.getData());
                Log.d(TAG, "onEvent: " + value.getData());
            }
        });
    }


//    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//        @Override
//        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                assert document != null;
//                if (document.exists()) {
//                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
//                    singleUserDocument.postValue(document.getData());
//                } else {
//                    Log.d(TAG, "No such document");
//                }
//            } else {
//                Log.d(TAG, "get failed with ", task.getException());
//            }
//        }
//    });

    public void loadUsersDocument() {

        final ArrayList<Map> mDataset = new ArrayList<>();

        db.collection("Users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful())
                            {
                                QuerySnapshot document = task.getResult();

                                if(document == null)
                                {
                                    return;
                                }

                                for(QueryDocumentSnapshot data: document)
                                {
                                    mDataset.add(data.getData());
                                    Log.d(TAG, "onComplete: " + mDataset);
                                }

                                allUsersDocument.postValue(mDataset);

                            }
                        }
                    });
    }
}
