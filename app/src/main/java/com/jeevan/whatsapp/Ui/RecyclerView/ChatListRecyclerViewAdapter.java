package com.jeevan.whatsapp.Ui.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jeevan.whatsapp.Activities.GroupMessageActivity;
import com.jeevan.whatsapp.Activities.PrivateMessageActivity;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<ChatListRecyclerViewAdapter.MyViewHolder> implements Serializable {


    private final static String TAG = "ChatListReAdapter";

    private ArrayList<Map> dataList;
    private Context context;


    public ChatListRecyclerViewAdapter(ArrayList<Map> data, Context context)
    {
        this.dataList = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_list, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final String type = (String) dataList.get(position).get("type");


        if(type.equals("private"))
        {
            String profileImageSrc = String.valueOf(dataList.get(position).get(FeedDataEntry.PROFILE_IMAGE_CIRCLE_SOURCE));

            Log.d(TAG, "onBindViewHolder: " + profileImageSrc);
            holder.title.setText((String)dataList.get(position).get(FeedDataEntry.USERNAME));

            Picasso.get().load(profileImageSrc).placeholder(R.drawable.profile).into(holder.profileImage);
        }else{
            holder.title.setText((String) dataList.get(position).get("title"));
        }



        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("private"))
                {
                    sendToPrivateMessageActivity(dataList.get(position));
                }else{
                    sendToGroupMessageActivity(dataList.get(position));
                }
            }
        });

    }

    private void sendToGroupMessageActivity(Map map) {
        Intent intent = new Intent(context, GroupMessageActivity.class);
        intent.putExtra(FeedDataEntry.GROUP_TITLE, (String)map.get("title"));
        intent.putExtra(FeedDataEntry.GROUP_ID, (String) map.get("groupId"));
        context.startActivity(intent);
    }

    private void sendToPrivateMessageActivity(Map map) {
            Intent intent = new Intent(context, PrivateMessageActivity.class);
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

        private TextView title ;
        private CircleImageView profileImage;
        private LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.username_chat_list);
            profileImage = (CircleImageView) itemView.findViewById(R.id.user_image_chat_list);
            linearLayout = itemView.findViewById(R.id.chat_list_linearLayout);
        }

    }
}
