package com.sjsu.boreas.OnlineConnectionHandlers;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.Messages.MessageUtility;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.Database.Messages.ChatMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----MyFirebaseMessagingService";
    private LocalDatabaseReference localDb = LocalDatabaseReference.get();

    @Override
    public void onMessageReceived(RemoteMessage mssg) {
        super.onMessageReceived(mssg);
        Log.e(TAG, SUB_TAG+"On mssg received][][][][][][][][][][][][][][" + mssg.getData().get("body"));

        ChatMessage chatMssg = null;

        if(mssg.getData().get("body")==null){
            return;
        }else{
            chatMssg = MessageUtility.convertJsonToMessage(mssg.getData().get("body"));
            if(chatMssg != null)
//                newMessageReceived(chatMssg);
                localDb.saveChatMessageLocally(chatMssg);
        }

        Log.e(TAG, SUB_TAG+"<><><><><><><><><><> Leaving hera");
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

        if(MainActivity.currentUser != null) {
            //We are putting this data under the users branch of the firebase database
            firebase_child_update.put("/users/" + MainActivity.currentUser.getUid() + "/tokenFCM/", token);
            //Do the actual writing of the data onto firebase
            FirebaseController.getDatabaseReference().updateChildren(firebase_child_update);
        }
    }

    private void newMessageReceived(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"New message received and notifying Caht message");
        ChatMessage.notifyListener(mssg);
    }
}
