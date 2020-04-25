package com.sjsu.boreas.database.Messages;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.sjsu.boreas.DataChangeListeners.messageListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class ChatMessage implements Serializable {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----ChatMessage data class ";
    private static List<messageListener> listeners = new ArrayList<messageListener>();

    public enum ChatTypes {
        ONEONONEONLINECHAT(0),
        ONEONONEOFFLINECHAT(1),
        ONLINEGROUPCHAT(2),
        OFFLINEGROUPCHAT(3),
        GETMESSAGESFROMRADIO(4);

        private int value;
        private static Map map = new HashMap<>();

        private ChatTypes(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

    public ChatMessage(String mssgId, String mssgText,
                       String receiverId, String receiverName,
                       String senderId, String senderName,
                       double latitude, double longitude, long time, boolean isMyMssg, int mssgType){
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
        this.mssgType = mssgType;
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
    public long time;

    @ColumnInfo(name = "isMyMssg")
    public boolean isMyMssg;

    @ColumnInfo(name = "mssgType")
    public int mssgType;

//    public String toString(){
//        return name+": "+uid+"\n"+latitude + " , " + longitude+"\n" + (isMe ? "IS" : "NOT") + " me";
//    }

    public String toString(){
       String mssgStr = "{" +
                "mssgId: " + mssgId + ","
                +   "mssgText: " + mssgText + ","
                +   "receiverId: " + receiverId + ","
                +   "receiverName: " + receiverName + ","
                +   "senderId: " + senderId + ","
                +   "senderName: " + senderName + ","
                +   "latitude: " + String.valueOf(latitude) + ","
                +   "longtidue: " + String.valueOf(longitude) + ","
                +   "time: " + String.valueOf(time) + ","
                +   "isMyMssg: " + String.valueOf(isMyMssg) + ","
                +   "mssgType: " + String.valueOf(mssgType)
                + "} \n";
        return mssgStr;
    }

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
        result.put("mssgType", mssgType);
        return result;
    }

    public static void addMessageListener(messageListener listener){
        Log.e(TAG, SUB_TAG+"adding a listener");
        listeners.add(listener);
    }

    public static void notifySpecificListener(String senderId){
        Log.e(TAG, SUB_TAG+"notify specific user");
        for(int i = 0; i < listeners.size(); i++){
            if(listeners.get(i).getChatPartnerID().equals(senderId)){

            }
        }
    }

}
