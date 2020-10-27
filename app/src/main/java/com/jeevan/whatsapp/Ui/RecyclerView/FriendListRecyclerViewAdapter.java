package com.jeevan.whatsapp.Ui.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jeevan.whatsapp.Activities.FriendsProfileActivity;
import com.jeevan.whatsapp.Activities.GroupMessageActivity;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendListRecyclerViewAdapter extends RecyclerView.Adapter<FriendListRecyclerViewAdapter.MyViewHolder> implements Serializable {


    private final static String TAG = "FreindListReAdapter";

    private ArrayList<Map> dataList;
    private Context context;


    public FriendListRecyclerViewAdapter(ArrayList<Map> data, Context context)
    {
        this.dataList = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_show_in_list, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.username.setText((String) dataList.get(position).get("username"));
        holder.userBio.setText((String) dataList.get(position).get("profileBio"));

        String profileImageSrc =String.valueOf(dataList.get(position).get(FeedDataEntry.PROFILE_IMAGE_CIRCLE_SOURCE));

        Log.d(TAG, "onBindViewHolder: " + profileImageSrc);

        if(profileImageSrc != null)
        {
            Picasso.get().load(profileImageSrc).into(holder.profileImage);
        }

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToFriendProfileActivity(dataList.get(position));
            }
        });

    }

    private void sendToFriendProfileActivity(Map map) {
            Intent intent = new Intent(context, FriendsProfileActivity.class);
            intent.putExtra("hashMap", (Serializable) map);
            context.startActivity(intent);
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

        private TextView username, userBio ;
        private CircleImageView profileImage;
        private LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username_find_friends_list);
            userBio = (TextView)itemView.findViewById(R.id.userBio_find_friends_list);
            profileImage = (CircleImageView) itemView.findViewById(R.id.user_image_find_friends_list);
            linearLayout = itemView.findViewById(R.id.find_friends_linearLayout);
        }

    }
}
