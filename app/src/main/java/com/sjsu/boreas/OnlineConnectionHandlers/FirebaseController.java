package com.sjsu.boreas.OnlineConnectionHandlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.boreas.ChatView.MediaFilesRecyclerItems.FileItem;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Misc.ContextHelper;
import com.sjsu.boreas.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseController { //This is class should be used to access firebase database ref in any other activity (so we don't have
    //make hecka instances firebase objects everywhere for singular tasks )

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----FirebasedataRefAndInstance--- ";

    private static DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();
    private static StorageReference storage_ref = FirebaseStorage.getInstance().getReference();
    private static ContextHelper contextHelper = ContextHelper.get();
    private static LoggedInUser loggedInUser = null;
    private static LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    public static DatabaseReference getDatabaseReference(){
        Log.e(TAG, SUB_TAG+"get data ref");
        return database_ref;
    }

    public static StorageReference getStorageReference(){
        Log.e(TAG, SUB_TAG+"get storage ref");
        return storage_ref;
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

    public static void pushFirebaseToken(final User user){
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

    //This function is called for sending a oneOnOne message
    public static void pushMessageToFirebase(ChatMessage chatMessage, Context mActivity){
        Log.e(TAG, SUB_TAG+"Push message to firebase");
        if(chatMessage.contains_img)
            pushMessageMediaToFirebaseStorage(chatMessage, mActivity);
        else
            pushMessageToFirebaseDatabase(chatMessage);
    }

    private static void pushMessageToFirebaseDatabase(ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Pushing the message to firebase database");
        String oneOnOneChatId = "";


        String firstUser = MainActivity.currentUser.getUid(); // private key sent?
        String secondUser = chatMessage.recipient.getUid();

        Map<String, Object> new_chat_mssg = chatMessage.toMap();

        Map<String, Object> firebase_child_update = new HashMap<>();

        if(getOneOnOneChatFirebaseID(firstUser, secondUser)){
            // TODO: change this dumbdumbydumbdumb to einstein
            oneOnOneChatId = firstUser + secondUser;
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user1", firstUser);
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user2", secondUser);
        }
        else{
            oneOnOneChatId = secondUser + firstUser;
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user1", secondUser);
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user2", firstUser);
        }

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/messages/" + chatMessage.mssgId, new_chat_mssg);

        Log.e(TAG, SUB_TAG+"--------\n\n\t------");
        //Do the actual writing of the data onto firebase
        database_ref.updateChildren(firebase_child_update);

    }

    private static void pushMessageMediaToFirebaseStorage(final ChatMessage chatMessage, final Context mActivity){
        Log.e(TAG, SUB_TAG+"Pusing mssg media to firebase");
        Bitmap bitmap = FileItem.stringToBitMap(chatMessage.imgData);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        storage_ref.child("/images/"+"oneOnOneChats/"+chatMessage.mssgId+".jpg").putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot){
                        // Image uploaded successfully
                        // Dismiss dialog
                        Toast .makeText(mActivity, 	"Image Uploaded!!", Toast.LENGTH_SHORT) .show();

                        //Send the message to firebase database once the image is done uplodaing
                        //The local uri shouldne be stored in the firbase database
                        chatMessage.imgUri = "";
                        chatMessage.imgData = "";
                        pushMessageToFirebaseDatabase(chatMessage);
                    }}
                ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error, Image not uploaded
                        Toast.makeText(mActivity,"Failed " + e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //This message creates a unique id which will be used to identify the chat between the 2 users
    //  to whom the 2 ID's belong to
    private static boolean getOneOnOneChatFirebaseID(String user1ID, String user2ID){
        Log.e(TAG, SUB_TAG+"getting the ID to be used on firebase");
        if((user2ID.compareTo(user1ID)) > 0){
            Log.e(TAG, SUB_TAG+"This is the id: " + user1ID+user2ID);
            return true;
        }
        Log.e(TAG, SUB_TAG+"This is the id: " + user2ID+user1ID);
        return false;
    }

    //This function downloads the image from Firebase Storage and returns the uri of the file
    //  where the image is downloaded to locally
    public static String downloadImageAndGetUri(final ChatMessage chatMssg){
        Log.e(TAG, SUB_TAG+"Loading media from storage for the given mssg");
        ContextHelper contextHelper = ContextHelper.get();
        File path = new File(contextHelper.getApplicationContext().getFilesDir(), "Boreas" + File.separator + "Images");
        if(!path.exists()){
            path.mkdirs();
        }
        File outFile = new File(path, chatMssg.time + ".jpeg");
        final Uri uri = Uri.fromFile(outFile);

        storage_ref.child("images")
                    .child("oneOnOneChats")
                    .child(chatMssg.mssgId+".jpg")
                    .getFile(outFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Log.e(TAG, SUB_TAG+"File downloaded");
                                            chatMssg.imgUri = uri.toString();
                                            Log.e(TAG, SUB_TAG+"\n\t-----" + chatMssg.imgUri);
                                            localDatabaseReference.saveChatMessageLocally(chatMssg);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.e(TAG, SUB_TAG+"\n"+exception);
                                            chatMssg.imgUri = null;
                                            //TODO: Save the message but save with a place holder "pic couldn't be loaded" image
                                        }
                                    });
        Log.e(TAG, SUB_TAG+"\n\t:" + chatMssg.imgUri);
        return chatMssg.imgUri;
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
//                            localDatabaseReference.wipeAllPreviousUserData();
                            localDatabaseReference.registerUser(loggedInUser);
//                            synchContactsForUser(loggedInUser, context);
                        }else{
                            Log.e(TAG, SUB_TAG+"The provide ()()()()()() password wrong.");
                        }
                    }
                    else{
                        Log.e(TAG, SUB_TAG+"user is actually empty dude !!!!!!!!!!!!!!!");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return loggedInUser;
    }

    public static void synchContactsForUser(LoggedInUser user, Context context){
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
