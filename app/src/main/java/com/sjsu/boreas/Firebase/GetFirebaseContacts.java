package com.sjsu.boreas.Firebase;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.sjsu.boreas.database.User;

import java.util.List;

public class GetFirebaseContacts {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---GetFirebaseContacts ";

    private DatabaseReference database_ref;

    public GetFirebaseContacts(){
        Log.e(TAG, SUB_TAG+"GetFireBase contacts constructor");

    }

    public User[] getMyFirebaseContacts(){
        Log.e(TAG, SUB_TAG+"Get my contacts");
        User[] usersContacts = null;

        return usersContacts;
    }
}
