package com.jeevan.whatsapp.Data;

import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.FieldValue;

import java.util.Calendar;
import java.util.Map;

public class Message {
    private String message;
    private Object timeAdded;
    private String messageAdminId;
    private String receiverId;
    private String messageType;

    public Message(){
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageType() {
        return messageType;
    }

    public Object getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Object timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageAdminId() {
        return messageAdminId;
    }

    public void setMessageAdminId(String messageAdminId) {
        this.messageAdminId = messageAdminId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
