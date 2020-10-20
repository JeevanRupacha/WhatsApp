package com.jeevan.whatsapp.Ui.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jeevan.whatsapp.Activities.GroupActivity;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class GroupListRecyclerViewAdapter extends RecyclerView.Adapter<GroupListRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Map> dataList;
    private Context context;

    private final static String TAG = "RecyclerViewAdapter";

    public GroupListRecyclerViewAdapter(ArrayList<Map> data, Context context)
    {
        this.dataList = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list, parent, false);
        Log.d(TAG, "GroupListRecyclerViewAdapter: ");

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: dataList" + dataList);
        Log.d(TAG, "onBindViewHolder: DataTest  "+Objects.requireNonNull(dataList.get(position).get("groupTitle")).toString());
      holder.groupTitle.setText(Objects.requireNonNull(dataList.get(position).get("groupTitle")).toString());

      holder.listCardView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              sendToGroupActivity(dataList.get(position));
          }
      });
    }


    private void sendToGroupActivity(Map map) {
        Intent intent = new Intent(context, GroupActivity.class);
            if(map.get(FeedDataEntry.GROUP_TITLE) != null && map.get(FeedDataEntry.GROUP_ID) != null){
            intent.putExtra(FeedDataEntry.GROUP_TITLE,map.get(FeedDataEntry.GROUP_TITLE).toString());
            intent.putExtra(FeedDataEntry.GROUP_ID, map.get(FeedDataEntry.GROUP_ID).toString());
        }
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

        private TextView groupTitle ;
        private CardView listCardView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = (TextView) itemView.findViewById(R.id.group_title_name);
            listCardView = (CardView)itemView.findViewById(R.id.group_list_cardView);
        }

    }
}
