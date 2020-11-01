package com.sjsu.boreas.OnlineConnectionHandlers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

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
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.Misc.ContextHelper;
import com.sjsu.boreas.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseController { //This is class should be used to access firebase database ref in any other activity (so we don't have
    //make hecka instances firebase objects everywhere for singular tasks )

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----FirebasedataRefAndInstance--- ";

    private static DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();
    private static ContextHelper contextHelper = ContextHelper.get();
    private static LoggedInUser loggedInUser = null;
    private static LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    public static DatabaseReference getDatabaseReference(){
        Log.e(TAG, SUB_TAG+"get data ref");
        return database_ref;
    }

    private static void RegisterUserOnFirebase(User user){
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

    public static void pushNewUserToFIrebase(User myUser, Context context){
        Log.e(TAG, SUB_TAG+"Push new user to firebase");
        boolean connected = false;

        if(networkIsAvailable()) {
            Log.e(TAG, SUB_TAG+"Network is available: so pushing to firebase");
            FirebaseController.RegisterUserOnFirebase(myUser);
        }
        else{
            Log.e(TAG, SUB_TAG+"NEtwork isn't available");
            showNetworkErrorMessage(context);
        }
    }

    private static boolean networkIsAvailable(){
        Log.e(TAG, SUB_TAG+"inside function networkIsAvailable");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) contextHelper.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static void showNetworkErrorMessage(Context context){
        Log.e(TAG, SUB_TAG+"show network error message");
        Toast.makeText(context, "Network not available, can't access online DB.", Toast.LENGTH_SHORT).show();
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
    public static LoggedInUser checkLogInInfo(final String userID, final String password, final Context context){
        Log.e(TAG, SUB_TAG+"Checking the provided user ID and password on Firebase");

        if(!networkIsAvailable()){
            showNetworkErrorMessage(context);
            return null;
        }

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
                                snapshot.child("password").getValue().toString(),
                                snapshot.child("publicKey").getValue().toString(),
                                snapshot.child("privateKey").getValue().toString());
                        users.add(user);
                    }

                    if(!users.isEmpty()){
                        LoggedInUser user = users.get(0);
                        if(user.getPassword().equals(password)){
                            Log.e(TAG, SUB_TAG+"The login info was correct");
                            loggedInUser = user;
                            synchContactsForUser(loggedInUser, context);
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

    private static void synchContactsForUser(LoggedInUser user, Context context){
        Log.e(TAG, SUB_TAG+"Synch the contacts for the provided user");

        if(!networkIsAvailable()){
            showNetworkErrorMessage(context);
            return;
        }

        database_ref.child("contacts").child(user.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, SUB_TAG + "On data Change listener");
                ArrayList<User> contacts = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User contact = new User(snapshot.child("uid").getValue().toString(),
                                snapshot.child("name").getValue().toString(),
                                Double.parseDouble(snapshot.child("latitude").getValue().toString()),
                                Double.parseDouble(snapshot.child("longitude").getValue().toString()),
                                snapshot.child("publicKey").getValue().toString());
                        Log.e(TAG, SUB_TAG+"\n\t"+contact.name);
                        localDatabaseReference.addContact(contact);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
