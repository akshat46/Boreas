package com.sjsu.boreas.Firebase;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.database.Messages.ChatMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage mssg) {
        super.onMessageReceived(mssg);
        Log.e(TAG, SUB_TAG+"On mssg received][][][][][][][][][][][][][][" + mssg.getData().get("mssgText"));

        Map<String, String> data = mssg.getData();

        ChatMessage newMssg = new ChatMessage(mssg.getData().get("mssgId"), mssg.getData().get("mssgText"),
                                mssg.getData().get("receiverId"), mssg.getData().get("receiverName"),
                                mssg.getData().get("senderName"), mssg.getData().get("senderId"),
                                Double.parseDouble(mssg.getData().get("latitude")), Double.parseDouble(mssg.getData().get("longtitude")),
                                Integer.parseInt(mssg.getData().get("time")), Boolean.parseBoolean(mssg.getData().get("isMyMssg")), Integer.parseInt(mssg.getData().get("mssgType")));
        Log.e(TAG, SUB_TAG+"New mssg: "+ newMssg.receiverName + ", mssgType: " + newMssg.mssgType);

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
}
