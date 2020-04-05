package com.sjsu.boreas.Firebase;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDataRefAndInstance { //This is class should be used to access firebase database ref in any other activity (so we don't have
    //make hecka instances firebase objects everywhere for singular tasks )

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----FirebasedataRefAndInstance";

    private static DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();

    public static DatabaseReference getDatabaseReference(){
        Log.e(TAG, SUB_TAG+"get data ref");
        return database_ref;
    }
}
