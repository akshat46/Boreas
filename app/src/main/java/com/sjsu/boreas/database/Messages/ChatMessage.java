package com.sjsu.boreas.database.Messages;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatMessage implements Serializable {

    public ChatMessage(String mssgId, String mssgText, String receiverId, String receiverName,
                       String senderName, String senderId, double latitude, double longitude, String time, boolean isMyMssg){
        this.mssgId = mssgId;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.senderName = senderName;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.mssgText = mssgText;
        this.isMyMssg = isMyMssg;
    }

    public String getSenderName(){    return senderName;}

    @NonNull
    @PrimaryKey
    public String mssgId;

    @ColumnInfo(name = "mssgText")
    public String mssgText;

    @ColumnInfo(name = "receiverId")
    public String receiverId;

    @ColumnInfo(name = "receiverName")
    public String receiverName;

    @ColumnInfo(name = "senderName")
    public String senderName;

    @ColumnInfo(name = "senderId")
    public String senderId;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "isMyMssg")
    public boolean isMyMssg;


//    public String toString(){
//        return name+": "+uid+"\n"+latitude + " , " + longitude+"\n" + (isMe ? "IS" : "NOT") + " me";
//    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("mssgId", mssgId);
        result.put("mssgText", mssgText);
        result.put("receiverId", receiverId);
        result.put("receiverName", receiverName);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("time", time);
        result.put("isMyMssg", isMyMssg);
        return result;
    }

}
