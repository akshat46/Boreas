package com.sjsu.boreas.Database.Messages;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MessageUtility {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------MessageUtility ";

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

//            ContextHelper contextHelper = ContextHelper.get(null);
//            DatabaseReference databaseReference = DatabaseReference.get(contextHelper.getApplicationContext());
//            databaseReference.saveChatMessageLocally(mssg); //Save chat message
//
//            //Save the sender's info to the database
//            User sender = new User(senderId, senderName, latitude, longitude, false);
//            if(databaseReference.isUserAlreadyInContacts(sender)){
//                Log.e(TAG, SUB_TAG+"The user is already in contacts");
//            }else {
//                //TODO: this user shouldn't be added to contacts,
//                // the person should be added to a different table of potential contacts
//                // before he/she is verified as a contact by the app owner
//                databaseReference.addContact(sender);
//            }

            Log.e(TAG, SUB_TAG+"New mssg: "+ mssg.receiverName + ", mssgType: " + mssg.mssgType);
        } catch (JSONException e) {
            Log.e(TAG, SUB_TAG+"JSON exception: \n\t" + e);
            e.printStackTrace();
        }

        return mssg;
    }

    public static ChatMessage convertHashMapToChatMessage(HashMap<String, Object> mssg){
        Log.e(TAG, SUB_TAG+"converting a hash map to ChatMessage object.");

        ChatMessage mssgObj = new ChatMessage();

        mssgObj.isMyMssg = (boolean) mssg.get("isMyMssg");
        mssgObj.latitude = (double) mssg.get("latitude");
        mssgObj.longitude = (double) mssg.get("longitude");
        mssgObj.mssgId = (String) mssg.get("mssgId");
        mssgObj.receiverId = (String) mssg.get("receiverId");
        mssgObj.senderId = (String) mssg.get("senderId");
        mssgObj.receiverName = (String) mssg.get("receiverName");
        mssgObj.senderName = (String) mssg.get("senderName");
        mssgObj.time = (long) mssg.get("time");
        mssgObj.mssgType = (int) mssg.get("mssgType");
        mssgObj.mssgText = (String) mssg.get("mssgText");

        return mssgObj;

    }

}
