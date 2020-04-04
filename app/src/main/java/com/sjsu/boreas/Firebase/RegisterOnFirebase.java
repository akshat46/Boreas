package com.sjsu.boreas.Firebase;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    }

}
