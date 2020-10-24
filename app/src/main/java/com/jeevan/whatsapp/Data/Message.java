package com.jeevan.whatsapp.Data;

public class Message {
    private String message;
    private long timeAdded;
    private String messageAdminId;

    public Message(){}

    public Message(String message, long timeAdded) {
        this.message = message;
        this.timeAdded = timeAdded;
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

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }
}
