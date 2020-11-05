package com.jeevan.whatsapp.Ui.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jeevan.whatsapp.Activities.PrivateMessageActivity;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Fragments.ContactFragment;
import com.jeevan.whatsapp.R;
import com.jeevan.whatsapp.WhatsAppMVVC.WhatsAppDataModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;


public class ContactListRecyclerViewAdapter extends RecyclerView.Adapter<ContactListRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Map> dataList;
    private Context context;

    private final static String TAG = "ContactRecyclerView";

    //firebase firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public ContactListRecyclerViewAdapter(ArrayList<Map> data, Context context)
    {
        this.dataList = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_single_list, parent, false);
        Log.d(TAG, "GroupListRecyclerViewAdapter: ");

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        final String number = dataList.get(position).get(FeedDataEntry.CONTACT_NUMBER).toString()
                .replaceAll("-","")
                .replaceAll(" ","")
                .replaceAll("\\(","")
                .replaceAll("\\)","");

        holder.contactName.setText(dataList.get(position).get(FeedDataEntry.CONTACT_NAME).toString());
        holder.contactNumber.setText(number);

        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            QuerySnapshot snapshot = task.getResult();

                            for (final DocumentSnapshot user: snapshot)
                            {
                                Log.d(TAG, "onComplete: " +user.getData().get("userPhoneNumber") + "to match " + number);
                                if(user.getData().get("userPhoneNumber") != null && user.getData().get("userPhoneNumber").equals(number))
                                {
                                    Log.d(TAG, "onComplete: Matched");
                                    holder.messageButton.setVisibility(View.VISIBLE);
                                    holder.messageButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(context, PrivateMessageActivity.class);
                                            intent.putExtra("hashMap", (Serializable) user.getData());
                                            intent.putExtra("phoneNumber",number);
                                            intent.putExtra("contactName",dataList.get(position).get(FeedDataEntry.CONTACT_NAME).toString());
                                            context.startActivity(intent);

                                        }


                                    });
                                    break;
                                }else{
                                    holder.messageButton.setVisibility(View.GONE);

                                }
                            }
                        }else{
                            Log.d(TAG, "onComplete: Fails to load");
                        }
                    }
                });




    }



    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + dataList.size());
        if(dataList == null) {
            return 0;
        }else {
            return dataList.size();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView contactName, contactNumber ;
        private CardView listCardView;
        private ImageButton messageButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            contactNumber = (TextView) itemView.findViewById(R.id.contact_phone_number);
            listCardView = (CardView)itemView.findViewById(R.id.contact_list_cardView);
            messageButton = itemView.findViewById(R.id.message_button_contact_list);
        }

    }
}
