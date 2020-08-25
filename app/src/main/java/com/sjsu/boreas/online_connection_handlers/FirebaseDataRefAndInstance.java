package com.sjsu.boreas.online_connection_handlers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sjsu.boreas.database.User;

import java.util.HashMap;
import java.util.Map;

public class FirebaseDataRefAndInstance { //This is class should be used to access firebase database ref in any other activity (so we don't have
    //make hecka instances firebase objects everywhere for singular tasks )

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----FirebasedataRefAndInstance";

    private static DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();

    public static DatabaseReference getDatabaseReference(){
        Log.e(TAG, SUB_TAG+"get data ref");
        return database_ref;
    }

    public static void RegisterUserOnFirebase(User user){
        Log.e(TAG, SUB_TAG+"RegisterUserOnFirebase");

        Map<String, Object> new_user = user.toMap();

        Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/users/" + user.getUid(), new_user);

        //Do the actual writing of the data onto firebase
        database_ref.updateChildren(firebase_child_update);

        //Firebase registration token
        pushFirebaseToken(user);

    }

    private static void pushFirebaseToken(final User user){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.e(TAG, SUB_TAG+"TOKKKKKKKKKKKKKKKKKKKKKKKKen: "+token);

                        //ALso update the FCM token
                        Map<String, Object> firebase_child_update = new HashMap<>();

                        //We are putting this data under the users branch of the firebase database
                        firebase_child_update.put("/users/" + user.getUid() + "/tokenFCM", token);

                        //Do the actual writing of the data onto firebase
                        database_ref.updateChildren(firebase_child_update);
                    }
                });
    }
}
