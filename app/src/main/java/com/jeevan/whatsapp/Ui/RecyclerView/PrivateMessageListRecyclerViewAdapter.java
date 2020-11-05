package com.jeevan.whatsapp.Ui.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jeevan.whatsapp.Activities.GroupMessageActivity;
import com.jeevan.whatsapp.Activities.PrivateMessageActivity;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.Firestore.MyFirestore;
import com.jeevan.whatsapp.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class PrivateMessageListRecyclerViewAdapter extends RecyclerView.Adapter<PrivateMessageListRecyclerViewAdapter.MyViewHolder> implements Serializable {


    private final static String TAG = "MessageListReAdapter";

    private ArrayList<Map> dataList;
    private Context context;
    private String profileImage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String userId = auth.getCurrentUser().getUid();


    public PrivateMessageListRecyclerViewAdapter(ArrayList<Map> data,String receivedMessageProfileImage, Context context)
    {
        this.dataList = data;
        this.context = context;
        this.profileImage = receivedMessageProfileImage;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_display_list, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        StringBuilder timeAgo = new StringBuilder();
        try
        {
            Date dt = new Date((Long) dataList.get(position).get("timeAdded"));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            String time = sdf.format(dt);
            timeAgo.append((String) DateUtils.getRelativeTimeSpanString((Long) dataList.get(position).get("timeAdded")));
            timeAgo.append(" , " + time);
        } catch (Exception e)
        {
            Log.d(TAG, "onBindViewHolder: " + e);
        }


//        holder.webView.setWebViewClient(new WebViewClient());
//        holder.webView.loadUrl("https://youtu.be/TUXui5ItBkM");
//

        if(dataList.get(position).get("messageType").equals("text"))
        {
            displayMessage("text",holder,position,timeAgo);
        }

//        if(dataList.get(position).get("messageType").equals("emoji"))
//        {
//            displayMessage("emoji",holder,position,timeAgo);
//
//        }

    }

    private void displayMessage(String messageSubType, MyViewHolder holder, final int position, StringBuilder timeAgo)
    {
        if(dataList.get(position).get("messageAdminId").equals(userId)){
            holder.sendMessage.setVisibility(View.VISIBLE);
            holder.sendMessageTextView.setText(String.valueOf(dataList.get(position).get("message")));
            holder.sendMessageTime.setText(timeAgo);
            holder.receivedMessage.setVisibility(View.GONE);
            holder.webViewLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            holder.videoLayout.setVisibility(View.GONE);

            if(position>0 && dataList.get(position-1).get(FeedDataEntry.MESSAGE_ADMIN_ID).equals(dataList.get(position).get(FeedDataEntry.MESSAGE_ADMIN_ID))){
                holder.sendMessageBoxLayout.setBackgroundResource(R.drawable.send_message_back_rect);
            }



            holder.sendMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View alertView = inflater.inflate(R.layout.delete_message_popup,null);
                    builder.setView(alertView);

                    //Show alert
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.show();

                    alertView.findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(profileImage == "group")
                            {
                                GroupMessageActivity.deleteMessage((String)dataList.get(position).get("messageKey"));
                            }else{

                                new MyFirestore((String)dataList.get(position).get("receiverId"),(String)dataList.get(position).get("messageKey")).deleteMessage();
                            }
                            alertDialog.dismiss();
                        }
                    });

                    alertView.findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.cancel();
                        }
                    });
                    return true;
                }
            });



        }else {
            holder.receivedMessage.setVisibility(View.VISIBLE);
            holder.receivedMessageTextView.setText(String.valueOf(dataList.get(position).get("message")));
            holder.receivedMessageTime.setText(timeAgo);
            holder.webViewLayout.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            holder.videoLayout.setVisibility(View.GONE);
            holder.sendMessage.setVisibility(View.GONE);

            Log.d(TAG, "displayMessage: test");

            //position>0 is bug because first item should be display the image

            if (position > 0 && dataList.get(position - 1).get(FeedDataEntry.MESSAGE_ADMIN_ID).equals(dataList.get(position).get(FeedDataEntry.MESSAGE_ADMIN_ID))) {
                holder.profileImage.setVisibility(View.INVISIBLE);
                holder.receivedMessageBoxLayout.setBackgroundResource(R.drawable.received_message_back_rect);
            }else{
                /**
                 *Check if porfile image value is group then it is from group message activity
                 * We have to hide user information and photo instead use
                 * anonymous photo
                 *
                 * else the message is private and show user photo
                 */
                if(profileImage == "group"){
                    Picasso.get().load(R.drawable.anonymous).into(holder.profileImage);
                }else{
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(holder.profileImage);
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        if(dataList == null) {
            return 0;
        }else {
            return dataList.size();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView receivedMessageTextView,receivedMessageTime,sendMessageTextView,sendMessageTime ;
        private CircleImageView profileImage;
        private LinearLayout rootLayout;

        private LinearLayout sendMessage, receivedMessage, receivedMessageBoxLayout;
        private LinearLayout sendMessageBoxLayout;

        //Image
        private LinearLayout imageLayout;
        private ImageView imageView;
        private TextView imageSenderName;

        //video
        private LinearLayout videoLayout;
        private VideoView videoView;
        private TextView videoSenderName;

        //webView
        private LinearLayout webViewLayout;
        private WebView webView;
        private TextView webViewSenderName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMessageTextView = itemView.findViewById(R.id.received_message_box);
            receivedMessageTime = itemView.findViewById(R.id.received_message_time);
            sendMessageTextView = itemView.findViewById(R.id.send_message_box);
            sendMessageTime = itemView.findViewById(R.id.send_message_time);
            profileImage =  itemView.findViewById(R.id.message_box_user_image);
            rootLayout = itemView.findViewById(R.id.message_display_root_layout);

            receivedMessage = itemView.findViewById(R.id.received_message);
            sendMessage = itemView.findViewById(R.id.send_message);
            receivedMessageBoxLayout = itemView.findViewById(R.id.received_message_box_layout);
            sendMessageBoxLayout = itemView.findViewById(R.id.send_message_box_layout);

            //image
            imageView = itemView.findViewById(R.id.chat_message_imageView);
            imageSenderName = itemView.findViewById(R.id.image_sender_name);
            imageLayout = itemView.findViewById(R.id.chat_message_image_layout);

            //video
            videoView = itemView.findViewById(R.id.chat_message_videoView);
            videoSenderName = itemView.findViewById(R.id.video_sender_name);
            videoLayout = itemView.findViewById(R.id.chat_message_video_layout);

            //webView
            webView = itemView.findViewById(R.id.message_webView);
            webViewSenderName= itemView.findViewById(R.id.webview_sender_name);
            webViewLayout = itemView.findViewById(R.id.chat_message_webView_layout);
        }

    }
}
