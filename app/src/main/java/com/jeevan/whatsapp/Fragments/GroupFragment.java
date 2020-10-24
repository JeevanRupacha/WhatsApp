package com.jeevan.whatsapp.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Data.GroupData;
import com.jeevan.whatsapp.R;
import com.jeevan.whatsapp.Ui.RecyclerView.GroupListRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class GroupFragment extends Fragment implements View.OnClickListener {

    private static final int NUMS_GRID_COLS = 2;
    //Fields
    private ImageButton addGroupButton;
    private Button createNewButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter<GroupListRecyclerViewAdapter.MyViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private static final String TAG = "GroupFragment";


    //fields for groups
    private String groupName, groupCode;

    //group list data
    private ArrayList<Map> mDataset = new ArrayList<>();


    //firebase setup
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();
        //get all the group data associated with this account from firestore
        retrieveGroupData();

        addGroupButton.setOnClickListener(this);
        createNewButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpRecyclerView();
    }

    private void initializeFields() {
        addGroupButton = getView().findViewById(R.id.add_group_imagebutton);
        createNewButton = getView().findViewById(R.id.create_new_group_button);
        recyclerView = getView().findViewById(R.id.group_list_recyclerview);
    }

    private void setUpRecyclerView() {

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new GridLayoutManager(getContext(), NUMS_GRID_COLS);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        Log.d(TAG, "setUpRecyclerView: "+ mDataset);
        mAdapter = new GroupListRecyclerViewAdapter(mDataset, getContext());
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }

    private void retrieveGroupData() {
        DocumentReference docRef = db.collection("Users")
                .document(auth.getCurrentUser().getUid());

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    List<String> groupIdList = (List<String>) snapshot.getData().get(FeedDataEntry.GROUP_LIST);
                    assert groupIdList != null;
                    for (String groupId : groupIdList)
                    {
                        mDataset.clear();
                        getGroup(groupId);
                    }

                    Log.d(TAG, "onComplete: groupList "+ groupIdList);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

    }

    private void getGroup(String groupId) {
        DocumentReference docRef = db.collection("Groups")
                .document(groupId);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    mDataset.add(snapshot.getData());
                    setUpRecyclerView();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.add_group_imagebutton: addGroupPopupDialog();
            break;
            case R.id.create_new_group_button:
                createAlertDialog();
                break;
            default:break;
        }
    }

    private void addGroupPopupDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View alertView = getLayoutInflater().inflate(R.layout.group_add_popup, null);
        builder.setView(alertView);

        //Show alert
        final AlertDialog alertDialog = builder.show();

        final EditText groupCodeInput = alertView.findViewById(R.id.input_group_code);

        alertView.findViewById(R.id.submit_group_popup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String groupCodeVal = groupCodeInput.getText().toString();
                if(!TextUtils.isEmpty(groupCodeVal))
                {

                    try {
                        addGroup(groupCodeVal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    alertDialog.dismiss();
                }else
                {
                    Toast.makeText(getActivity(), "Fields should not be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertView.findViewById(R.id.cancel_group_popup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void createNewGroup() throws Exception {
        GroupData groupData = new GroupData();

        groupData.setGroupCode(groupCode);
        groupData.setGroupTitle(groupName);
        groupData.setGroupCreatedDate(System.currentTimeMillis());
        groupData.setAdminId(auth.getCurrentUser().getUid());

        final String randGroupId = createTransactionID();
        groupData.setGroupId(randGroupId);

        db.collection("Groups")
                .document(randGroupId)
                .set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    addGroupCodeList(randGroupId);
                    addGroupMemberList(randGroupId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Fail to inset in groups data");
            }
        });


    }

    private void addGroupMemberList(String groupId) {
        /////update the arraylist of group members list in Groups document
        DocumentReference groupListIdRef = db.collection("Groups")
                .document(groupId);

        groupListIdRef.update("members", FieldValue.arrayUnion(auth.getCurrentUser().getUid()));
        mAdapter.notifyDataSetChanged();

        Toast.makeText(getActivity(), "Success! member added in group", Toast.LENGTH_SHORT).show();
    }

    private void createAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View alertView = getLayoutInflater().inflate(R.layout.group_create_popup, null);
        builder.setView(alertView);

        //Show alert
        final AlertDialog alertDialog = builder.show();

        final EditText groupNameInput = alertView.findViewById(R.id.input_group_name);
        final EditText groupCodeInput = alertView.findViewById(R.id.input_group_code);

        alertView.findViewById(R.id.submit_group_popup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupNameVal = groupNameInput.getText().toString();
                String groupCodeVal = groupCodeInput.getText().toString();
                if(!TextUtils.isEmpty(groupCodeVal) && !TextUtils.isEmpty(groupNameVal))
                {
                    groupName = groupNameVal;
                    groupCode = groupCodeVal;

                    try {
                        createNewGroup();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    alertDialog.dismiss();
                }else
                {
                    Toast.makeText(getActivity(), "Fields should not be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertView.findViewById(R.id.cancel_group_popup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
    }

    private void addGroup(final String groupCodeVal) {

        //find the group with the code

        db.collection("Groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot snapshot: Objects.requireNonNull(task.getResult()))
                            {
                                if(snapshot.getData().get("groupCode") != null && Objects.requireNonNull(snapshot.getData().get("groupCode")).toString().equals(groupCodeVal))
                                {
                                    addGroupCodeList(snapshot.getId());
                                    addGroupMemberList(snapshot.getId());

                                    Log.d(TAG, "onComplete: "+ snapshot.getId() +"and " + snapshot.getData().get("groupCode"));
                                    return;
                                }else{
                                    Toast.makeText(getActivity(), "Code number is not found !", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Fail get groups data ");
            }
        });

    }

    private void addGroupCodeList(String groupId) {

        /////update the arraylist of group id list in user document
        DocumentReference groupListIdRef = db.collection("Users")
                .document(auth.getCurrentUser().getUid());

        groupListIdRef.update("groupList", FieldValue.arrayUnion(groupId))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: group code is added to user grouplist");
                    }
                });
        mAdapter.notifyDataSetChanged();

    }

    //https://stackoverflow.com/questions/40237169/generating-unique-id-in-android-uuid
    public String createTransactionID() throws Exception{
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }
}
