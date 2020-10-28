package com.jeevan.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Data.UserProfile;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;
import com.jeevan.whatsapp.Ui.RecyclerView.FriendListRecyclerViewAdapter;
import com.jeevan.whatsapp.Ui.RecyclerView.GroupListRecyclerViewAdapter;
import com.jeevan.whatsapp.WhatsAppMVVC.WhatsAppDataModel;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private static final String TAG = "FindFriendsActivity";



    //Fields variables
    private RecyclerView recyclerView;
    private FriendListRecyclerViewAdapter mAdapter;
    private ArrayList<Map> mDataset;

    private ProgressBar progressBar;


    //firebase setup
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    //android MVVC
    private WhatsAppDataModel whatsAppDataModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        setUpToolbar();
        initializeFields();

        whatsAppDataModel.getUsersDocument().observe(this, new Observer<ArrayList<Map>>() {
            @Override
            public void onChanged(ArrayList<Map> usersArrayList) {
                mDataset.addAll(usersArrayList);
                setUpRecyclerView();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        visibleSpinBar();
    }

    private void visibleSpinBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void initializeFields()
    {
        recyclerView = findViewById(R.id.find_friends_recyclerView);
        progressBar = findViewById(R.id.find_friends_progressBar);
        mDataset = new ArrayList<>();

        //android jetPack viewModel live data component architecture
        whatsAppDataModel = new ViewModelProvider(this).get(WhatsAppDataModel.class);
    }

    private void setUpRecyclerView() {

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        Log.d(TAG, "setUpRecyclerView: "+ mDataset);
        mAdapter = new FriendListRecyclerViewAdapter(mDataset, this);
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);

        //remove spin bar after load data
        progressBar.setVisibility(View.GONE);
    }



    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        toolbar.setTitle(R.string.friend_lists);
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
            startActivity(new Intent(FindFriendsActivity.this, MainActivity.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}