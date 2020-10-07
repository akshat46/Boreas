package com.sjsu.boreas.OnlineConnectionHandlers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataRefAndInstance { //This is class should be used to access firebase database ref in any other activity (so we don't have
    //make hecka instances firebase objects everywhere for singular tasks )

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----FirebasedataRefAndInstance";

    private static DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();
    private static LoggedInUser loggedInUser = null;

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

    public static void addContact(User user){
        Log.e(TAG, SUB_TAG+"adding contact to firebase");

        Map<String, Object> new_user = user.toMap();
        final Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/contacts/" + MainActivity.currentUser.getUid() + "/" + user.getUid(), new_user);
        Log.e(TAG, SUB_TAG+"My user ID is: " + MainActivity.currentUser.getUid() + ", and the contact id is: " + user.getUid());

        //Do the actual writing of the data onto firebase and locally
        database_ref.updateChildren(firebase_child_update);
    }

    //If the user is found and the password matches then return true otherwise false
    public static LoggedInUser checkLogInInfo(final String userID, final String password){
        Log.e(TAG, SUB_TAG+"Checking the provided user ID and password on Firebase");

        database_ref.child("users").orderByChild("uid").equalTo(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, SUB_TAG + "On data Change listener");
                List<LoggedInUser> users = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LoggedInUser user = new LoggedInUser(snapshot.child("uid").getValue().toString(),
                                snapshot.child("name").getValue().toString(),
                                Double.parseDouble(snapshot.child("latitude").getValue().toString()),
                                Double.parseDouble(snapshot.child("longitude").getValue().toString()),
                                snapshot.child("password").getValue().toString());
                        users.add(user);
                    }

                    if(!users.isEmpty()){
                        LoggedInUser user = users.get(0);
                        if(user.getPassword().equals(password)){
                            Log.e(TAG, SUB_TAG+"The login info was correct");
                            loggedInUser = user;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return loggedInUser;
    }
}
