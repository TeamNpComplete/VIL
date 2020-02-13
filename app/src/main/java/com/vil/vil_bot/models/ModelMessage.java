package com.vil.vil_bot.models;

import java.util.Calendar;
import java.util.Date;

public class ModelMessage {
    String messageID;
    String text, intent, senderName, date;
    Object timestamp;

    public ModelMessage(){

    }

    public ModelMessage(String text, String intent, String senderName) {
        this.text = text;
        this.intent = intent;
        this.senderName = senderName;
        this.date = Calendar.getInstance().getTime().toString();
        this.timestamp = new Date().getTime();
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
