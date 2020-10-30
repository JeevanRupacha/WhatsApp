package com.jeevan.whatsapp.Ui.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.R;

import java.util.ArrayList;
import java.util.Map;


public class ContactListRecyclerViewAdapter extends RecyclerView.Adapter<ContactListRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Map> dataList;
    private Context context;

    private final static String TAG = "ContactRecyclerView";

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
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        holder.contactName.setText(dataList.get(position).get(FeedDataEntry.CONTACT_NAME).toString());
        holder.contactNumber.setText(dataList.get(position).get(FeedDataEntry.CONTACT_NUMBER).toString());

      holder.listCardView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
//              sendToMessageActivity(dataList.get(position));
          }
      });
    }


    private void sendToMessageActivity(Map map) {
//        Intent intent = new Intent(context, GroupMessageActivity.class);
//            if(map.get(FeedDataEntry.GROUP_TITLE) != null && map.get(FeedDataEntry.GROUP_ID) != null){
//            intent.putExtra(FeedDataEntry.GROUP_TITLE,map.get(FeedDataEntry.GROUP_TITLE).toString());
//            intent.putExtra(FeedDataEntry.GROUP_ID, map.get(FeedDataEntry.GROUP_ID).toString());
//        }
//        context.startActivity(intent);
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
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            contactName = (TextView) itemView.findViewById(R.id.contact_name);
            contactNumber = (TextView) itemView.findViewById(R.id.contact_phone_number);
            listCardView = (CardView)itemView.findViewById(R.id.contact_list_cardView);
        }

    }
}
