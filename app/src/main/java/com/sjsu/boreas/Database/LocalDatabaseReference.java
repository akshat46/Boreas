package com.sjsu.boreas.Database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.view.menu.SubMenuBuilder;
import androidx.room.Room;

import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.PotentialContacts.PotentialContacts;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Events.Event;
import com.sjsu.boreas.Events.EventEmitter;
import com.sjsu.boreas.Messages.LongDistanceMessage;
import com.sjsu.boreas.Notifications.CustomNotification;

import java.util.HashMap;
import java.util.List;

public class LocalDatabaseReference implements EventEmitter{

    private Event event_chatmessage = Event.get("chatmessages");
    private Event event_user = Event.get("user");

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------DatabaseReference----- ";

    private static LocalDatabaseReference localDatabaseReference = null;
    private AppDatabase database;
    private Context appContext = null;

    private CustomNotification customNotification = CustomNotification.get();

    public static LocalDatabaseReference initialize(Context context){
        Log.e(TAG, SUB_TAG+"getting instance");
        if(localDatabaseReference == null){
            localDatabaseReference = new LocalDatabaseReference(context);
            return localDatabaseReference;
        }else {
            return localDatabaseReference;
        }
    }

    private LocalDatabaseReference(Context context){
        Log.e(TAG, SUB_TAG+"Constructor");
        database = Room.databaseBuilder(context, AppDatabase.class, "mydatabase").build();
    }

    public static LocalDatabaseReference get(){
        Log.e(TAG, SUB_TAG+"getting the instance");
        return localDatabaseReference;
    }

    public void saveChatMessageLocally(final ChatMessage message){
        Log.e(TAG, SUB_TAG+"saving chat message locally");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, SUB_TAG+"Adding message to database" + message);
                if(!isMessageAlreadyInDatabase(message)) {
                    database.chatMessageDao().insertAll(message);
                    HashMap<String, Object> cm_map = (HashMap<String, Object>) message.toMap();
                    event_chatmessage.trigger(cm_map);
                    customNotification.sendMssgRecvdNotification(message);
                }
            }
        });

    }

    public void registerUser(final LoggedInUser user){
        Log.e(TAG, SUB_TAG+"save users locally");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, SUB_TAG+"Adding user to database" + user);
                //Delete the previous registered user (if there is one)
                if(userAlreadyIsRegistered(user)){
                    Log.e(TAG, SUB_TAG+"User is already registered");
                    return;
                }
                database.loggedInUserDao().clearLoggedInUserTable();
                database.loggedInUserDao().insertNewUser(user);
            }
        });
    }

    //Check if the given user is already registered or not
    private boolean userAlreadyIsRegistered(LoggedInUser user){
        Log.e(TAG, SUB_TAG+"check if user is already registered");

        List<LoggedInUser> allUsers = database.loggedInUserDao().getAllRegisteredUser();

        Log.e(TAG, SUB_TAG+"\n\t\tThese are all the registered users on the device: " + allUsers);

        LoggedInUser previousUser = null;
        previousUser = database.loggedInUserDao().checkIfUserIsAlreadyRegistered(user.getUid());

        if(previousUser != null){
            Log.e(TAG, SUB_TAG+"The user with the provided id is already registered");
            return true;
        }

        return false;
    }

    //This function at the moment needs to be called inside a Async thread (a sperate thread)
    public List<ChatMessage> getLastTwentyMessagesForSpecificUser(User specificUser){
        Log.e(TAG, SUB_TAG+"get last 20 mssgs for a specific user");
        return database.chatMessageDao().getLastTwentyMessagesForUser(specificUser.getUid(),
                ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());
    }

    //This function at the moment needs to be called inside a Async thread (a sperate thread)
    // like the way its being used in MainActivity
    public LoggedInUser getRegisteredUser(){
        Log.e(TAG, SUB_TAG+"getting the user of this app");
        LoggedInUser user = null;
        user = database.loggedInUserDao().getRegisteredUser();
        return user;
    }

    //The functions below gets the logged in user
    public LoggedInUser getLoggedInUser(){
        Log.e(TAG, SUB_TAG+"Get the logged in user");
        LoggedInUser loggedInUsers = null;
        loggedInUsers = database.loggedInUserDao().getLoggedInUser();
        return loggedInUsers;
    }

    public void logUserIn(String userID, String password){
        Log.e(TAG, SUB_TAG+"Login");
        LoggedInUser user = null;

        user = database.loggedInUserDao().getUserWithGivenCredentials(userID, password);
        if(user != null){
            Log.e(TAG, SUB_TAG+"The provided credentials are correct");
            user.logUserIn();
            Log.e(TAG, SUB_TAG+"--------------"+user);
            database.loggedInUserDao().logUserIn(user);
        }
    }

    public void logUserOut(LoggedInUser user){
        Log.e(TAG, SUB_TAG+"Loggin user out");
        user.logUserOut();
        database.loggedInUserDao().logUserOut(user);
    }

    public List<User> getContacts(){
        Log.e(TAG, SUB_TAG+"get contacts");
        return database.userDao().getUsers();
    }

    public void addContact(final User user){
        Log.e(TAG, SUB_TAG+"Saving contact locally");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!isUserAlreadyInContacts(user)) {
                    Log.e(TAG, SUB_TAG + "User doesn't already exist in the contacts and is being added now");
                    database.userDao().insertAll(user);
                }
            }
        });
    }

    public boolean isUserAlreadyInContacts(User user){
        Log.e(TAG, SUB_TAG+"Checking if user is already in contacts");
        if(!database.userDao().getSpecificUser(user.getUid()).isEmpty()){
            return true;
        }
        return false;
    }

    public List<User> getPotentialContacts(){
        Log.e(TAG, SUB_TAG+"get potential contacts");
        return database.potentialContactsDao().getUsers();
    }

    public void addPotentialContact(final PotentialContacts user){
        Log.e(TAG, SUB_TAG+"Saving the given user to the potential contact table");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!isUserAlreadyInPotentialContacts(user)) {
                    Log.e(TAG, SUB_TAG + "------User doesn't already exist in the potential contacts and is being added now");
                    database.potentialContactsDao().insertAll(user);
                }
            }
        });
    }

    public boolean isUserAlreadyInPotentialContacts(PotentialContacts user){
        Log.e(TAG, SUB_TAG+"Checking if user is already in potential contacts");
        if(!database.potentialContactsDao().getSpecificUser(user.getUid()).isEmpty()){
            return true;
        }
        return false;
    }

    public boolean isMessageAlreadyInDatabase(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Checking if the message is already in database");
        if(!database.chatMessageDao().getSpecificMessage(mssg.mssgId).isEmpty()){
            return true;
        }
        return false;
    }

    public List<User> getAllUsers(){
        Log.e(TAG, SUB_TAG+"Get all the users in the database");
        return database.userDao().getUsers();
    }

    public List<User> getClosestUseres(LongDistanceMessage message){
        Log.e(TAG, SUB_TAG+"get the closest users based on location");
        return database.userDao().getClosestUsers(message.recipient.latitude, message.recipient.longitude);
    }
}
