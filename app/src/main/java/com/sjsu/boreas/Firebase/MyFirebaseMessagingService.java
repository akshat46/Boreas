package com.sjsu.boreas.Firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.database.Messages.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage mssg) {
        super.onMessageReceived(mssg);
        Log.e(TAG, SUB_TAG+"On mssg received][][][][][][][][][][][][][][" + mssg.getData().get("body"));

        JSONObject jsonMssg = null;
        String mssgId, mssgText, receiverId, receiverName, senderId, senderName;
        double latitude, longitude;
        long time;
        int mssgType;
        boolean isMyMssg;

        //First get json object from string
        try {
            jsonMssg = new JSONObject(mssg.getData().get("body"));

            mssgId = jsonMssg.getString("mssgId");
            mssgText = jsonMssg.getString("mssgText");
            receiverId = jsonMssg.getString("receiverId");
            receiverName = jsonMssg.getString("receiverName");
            senderId = jsonMssg.getString("senderId");
            senderName = jsonMssg.getString("senderName");
            Log.e(TAG, SUB_TAG+"SO far so good");
            latitude = Double.parseDouble(jsonMssg.getString("latitude"));
            longitude = Double.parseDouble(jsonMssg.getString("longtitude"));
            Log.e(TAG, SUB_TAG+"SO far so good 2");
            time = Long.parseLong(jsonMssg.getString("time"));
            isMyMssg = Boolean.parseBoolean(jsonMssg.getString("isMyMssg"));
            mssgType = Integer.parseInt(jsonMssg.getString("mssgType"));

            ChatMessage newMssg = new ChatMessage(mssgId, mssgText,
                    receiverId, receiverName,
                    senderId, senderName,
                    latitude, longitude,
                    time, isMyMssg, mssgType);
            Log.e(TAG, SUB_TAG+"New mssg: "+ newMssg.receiverName + ", mssgType: " + newMssg.mssgType);
        } catch (JSONException e) {
            Log.e(TAG, SUB_TAG+"JSON exception: \n\t" + e);
            e.printStackTrace();
        }

        Log.e(TAG, SUB_TAG+"<><><><><><><><><><> Leaving hera");

        ChatMessage fakeMssg = new ChatMessage();

        newMessageReceived(fakeMssg);

    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, SUB_TAG+"Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/users/" + MainActivity.currentUser.getUid() + "/tokenFCM/", token);

        //Do the actual writing of the data onto firebase
        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);
    }

    private void newMessageReceived(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"New message received and notifying Caht message");
        ChatMessage.notifyAllListeners(mssg);
    }
}
