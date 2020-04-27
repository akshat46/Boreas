package com.sjsu.boreas.database.Messages;

import android.util.Log;

import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.database.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MessageHandler {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------MessageHandler ";

    public static ChatMessage convertJsonToMessage(String jsonStringMssg){
        ChatMessage mssg = null;
        JSONObject jsonMssg = null;

        String mssgId, mssgText, receiverId, receiverName, senderId, senderName;
        double latitude, longitude;
        long time;
        int mssgType;
        boolean isMyMssg;

        //First get json object from string
        try {
            jsonMssg = new JSONObject(jsonStringMssg);

            mssgId = jsonMssg.getString("mssgId");
            mssgText = jsonMssg.getString("mssgText");
            receiverId = jsonMssg.getString("receiverId");
            receiverName = jsonMssg.getString("receiverName");
            senderId = jsonMssg.getString("senderId");
            senderName = jsonMssg.getString("senderName");
            Log.e(TAG, SUB_TAG+"SO far so good: senderId: " + senderId);
            latitude = Double.parseDouble(jsonMssg.getString("latitude"));
            longitude = Double.parseDouble(jsonMssg.getString("longitude"));
            Log.e(TAG, SUB_TAG+"SO far so good 2");
            time = Long.parseLong(jsonMssg.getString("time"));
            isMyMssg = false;
            mssgType = Integer.parseInt(jsonMssg.getString("mssgType"));

            mssg = new ChatMessage(mssgId, mssgText,
                    receiverId, receiverName,
                    senderId, senderName,
                    latitude, longitude,
                    time, isMyMssg, mssgType);

            //We check if the message is already in database, if it is, don't save
            if(!messageAlreadyInDatabase(mssg))
                MainActivity.database.chatMessageDao().insertAll(mssg); //Save chat message

            //Save the sender's info to the database
            User sender = new User(senderId, senderName, latitude, longitude, false);
            MainActivity.database.userDao().insertNewUser(sender);

            Log.e(TAG, SUB_TAG+"New mssg: "+ mssg.receiverName + ", mssgType: " + mssg.mssgType);
        } catch (JSONException e) {
            Log.e(TAG, SUB_TAG+"JSON exception: \n\t" + e);
            e.printStackTrace();
        }

        return mssg;
    }

    private static boolean messageAlreadyInDatabase(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Checking if message is already received or not");

        List<ChatMessage> mssgList = MainActivity.database.chatMessageDao().getSpecificMessage(mssg.mssgId);

        if(mssgList.size() > 0)
            return true;

        return false;
    }
}
