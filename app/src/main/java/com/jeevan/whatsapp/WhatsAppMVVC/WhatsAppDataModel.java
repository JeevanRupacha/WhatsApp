package com.jeevan.whatsapp.WhatsAppMVVC;

import android.util.Log;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WhatsAppDataModel extends ViewModel {

    //Debug log string
    private static final String TAG = WhatsAppDataModel.class.getSimpleName();

    //firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    //users data for recycler view of friend list
    private MutableLiveData<ArrayList<Map>> allUsersDocument = new MutableLiveData<>();
    private MutableLiveData<Map> singleUserDocument = new MutableLiveData<>();
    private MutableLiveData<ArrayList<Map>> allUsersWithMessages = new MutableLiveData<>();

    public LiveData<ArrayList<Map>> getUsersDocument() {
        loadUsersDocument();
        return allUsersDocument;
    }

    public LiveData<Map> getSingleUserDocument(String userId) {
        loadSingleUserDocument(userId);
        return singleUserDocument;
    }

    public LiveData<ArrayList<Map>> getAllUsersWithMessages()
    {
        retrieveAllUsersHavingMessage();
        return allUsersWithMessages;
    }

    private void retrieveAllUsersHavingMessage() {
        final ArrayList<Map> mDataset = new ArrayList<>();

        db.collection("MessagesCollection")
                .document(auth.getCurrentUser().getUid())
                .collection("MyMessages")
                  .orderBy("timeAdded", Query.Direction.DESCENDING)
                  .addSnapshotListener(new EventListener<QuerySnapshot>() {
                      @Override
                      public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                          if(error != null)
                          {
                              Log.d(TAG, "onEvent: " + error);
                              return;
                          }

                          mDataset.clear();

                          for(QueryDocumentSnapshot documentUser: value)
                          {
                              DocumentReference docRef = db.collection("Groups")
                                      .document(documentUser.getId());

                              /**
                               * using addCompleteListener
                               */
//                                      docRef.get()
//                                              .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                                  @Override
//                                                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                      if(task.isSuccessful())
//                                                      {
//                                                          DocumentSnapshot value = task.getResult();
//                                                          if(value.exists())
//                                                          {
//                                                              Map<String, String > data = new HashMap<>();
//                                                              data.put("type", "group");
//                                                              data.put("title",(String) value.getData().get(FeedDataEntry.GROUP_TITLE));
//                                                              data.put("groupId", (String) value.getData().get(FeedDataEntry.GROUP_ID));
//
//                                                              mDataset.add(data);
//                                                              Log.d(TAG, "onComplete: "+ value.getData());
//                                                          }
//                                                      }
//                                                  }
//                                              });


                                      docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                          @Override
                                          public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                              if(error != null)
                                              {
                                                  Log.d(TAG, "onEvent: "+ error);
                                                  return;
                                              }
                                              if(value.exists())
                                              {
                                                  Map<String, String > data = new HashMap<>();
                                                  data.put("type", "group");
                                                  data.put("title",(String) value.getData().get(FeedDataEntry.GROUP_TITLE));
                                                  data.put("groupId", (String) value.getData().get(FeedDataEntry.GROUP_ID));

                                                  mDataset.add(data);
                                                  allUsersWithMessages.postValue(mDataset);

                                              }
                                          }
                                      });

                              db.collection("Users")
                                      .document(documentUser.getId())
                                      .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                          @Override
                                          public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                              if(error != null)
                                              {
                                                  Log.d(TAG, "onEvent: "+ error);
                                                  return;
                                              }
                                              if(value.exists())
                                              {
                                                  HashMap<String, String> data = new HashMap<>();
                                                  data.put("type", "private");
                                                  data.put("username",(String) value.getData().get(FeedDataEntry.USERNAME));
                                                  data.put("profileImageSrc", (String) value.getData().get("profileImageSrc"));
                                                  data.put("userID", (String) value.getData().get("userID"));
                                                  mDataset.add(data);
                                                  allUsersWithMessages.postValue(mDataset);
                                              }

                                          }
                                      });

                          }

                          Log.d(TAG, "onEvent: tt"+ mDataset);

                      }
                  });


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
