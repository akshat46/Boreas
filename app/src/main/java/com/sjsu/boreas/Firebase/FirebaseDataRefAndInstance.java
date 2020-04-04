package com.sjsu.boreas.Firebase;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataRefAndInstance {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----FirebasedataRefAndInstance";

    private static DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();

    public static DatabaseReference getDatabaseReference(){
        Log.e(TAG, SUB_TAG+"get data ref");
        return database_ref;
    }
}
