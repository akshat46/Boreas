package com.sjsu.boreas.Firebase;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.RegisterActivity;
import com.sjsu.boreas.database.User;

import java.util.HashMap;
import java.util.Map;

public class RegisterOnFirebase {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---RegisterOnFirebase ";

//    private DatabaseReference database_ref;

    public RegisterOnFirebase(){
        Log.e(TAG, SUB_TAG+"RegisterOnFirebase");
//        database_ref = FirebaseDatabase.getInstance().getReference();
    }

    public void RegisterUserOnFirebase(User user){
        Log.e(TAG, SUB_TAG+"RegisterUserOnFirebase");

        Map<String, Object> new_user = user.toMap();

        Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/users/" + user.getUid(), new_user);

        //Do the actual writing of the data onto firebase
        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);

        //Firebase registration token
        pushFirebaseToken(user);

    }

    private void pushFirebaseToken(final User user){
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
                        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);
                    }
                });
    }

}
