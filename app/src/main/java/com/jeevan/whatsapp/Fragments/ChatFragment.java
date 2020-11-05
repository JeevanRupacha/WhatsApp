package com.jeevan.whatsapp.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.R;
import com.jeevan.whatsapp.Ui.RecyclerView.ChatListRecyclerViewAdapter;
import com.jeevan.whatsapp.Ui.RecyclerView.FriendListRecyclerViewAdapter;
import com.jeevan.whatsapp.WhatsAppMVVC.WhatsAppDataModel;

import java.util.ArrayList;
import java.util.Map;

public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();
    //Fields variables
    private RecyclerView recyclerView;
    private ChatListRecyclerViewAdapter mAdapter;
    private ArrayList<Map> mDataset;


    //firebase setup
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    //android MVVC
    private WhatsAppDataModel whatsAppDataModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields();

        whatsAppDataModel.getAllUsersWithMessages().observe(getActivity(), new Observer<ArrayList<Map>>() {
            @Override
            public void onChanged(ArrayList<Map> dataArrayList) {
                mDataset.clear();
                mDataset.addAll(dataArrayList);
                Log.d(TAG, "onChanged: "+ dataArrayList);
                setUpRecyclerView();
            }
        });
    }


    private void initializeFields()
    {
        recyclerView = getView().findViewById(R.id.chat_fragment_recyclerView);
        mDataset = new ArrayList<>();

        //android jetPack viewModel live data component architecture
        whatsAppDataModel = new ViewModelProvider(this).get(WhatsAppDataModel.class);
    }

    private void setUpRecyclerView() {

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        Log.d(TAG, "setUpRecyclerView: "+ mDataset);
        mAdapter = new ChatListRecyclerViewAdapter(mDataset, getContext());
        recyclerView.setAdapter(mAdapter);

    }

}
