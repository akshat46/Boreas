package com.sjsu.boreas.ChatViewRelatedStuff;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.ChatViewRelatedStuff.MessageListViewStuff.OneOnOneMessageAdapter;
import com.sjsu.boreas.ChatViewRelatedStuff.MessageListViewStuff.OneOnOneMessageViewHolder;
import com.sjsu.boreas.Database.Messages.MessageUtility;
import com.sjsu.boreas.Events.Event;
import com.sjsu.boreas.Events.EventListener;
import com.sjsu.boreas.Events.messageListener;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.HelperStuff.ContextHelper;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseDataRefAndInstance;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.PhoneBluetoothRadio.BlueTerm;
import com.sjsu.boreas.R;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.Contacts.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity2 extends AppCompatActivity implements EventListener {

    private RecyclerView recyclerView;
    private View btnSend;
    private EditText mssgText;
    private TextView userName;
    private ArrayList<ChatMessage> chatMessages;
    private OneOnOneMessageAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private User myChatPartner;
    private Context mContext;
    private ChatActivity2 mActivity;

    public LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "----------------ChatActivity2 ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        Event event = Event.get(Event.chatMssgEventID);
        event.addListener(this);

        mContext = this;
        mActivity = this;

        Log.e(TAG, SUB_TAG+"We in hea on create");
        //Getting the user object from intent
        Intent intent = getIntent();
        myChatPartner = (User) intent.getSerializableExtra("ReceiverObj");
        userName = findViewById(R.id.userName);
        userName.setText(myChatPartner.name);


        Log.e(TAG, SUB_TAG+"-=-=-=-=-==-///////// "+myChatPartner+", and me: "+MainActivity.currentUser.getName());

        initUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(ContextHelper.get().getApplicationContext(),R.color.backgroundAlt));
        }

        mssgText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                    //Perform your Actions here.
                    Log.e(TAG, SUB_TAG+"We our here tryna detect enter key.");
                    if (mssgText.getText().toString().trim().equals("")) {
                        // TODO: disable button
                        Toast.makeText(ChatActivity2.this, "Please input some text...", Toast.LENGTH_SHORT).show();
                    } else {
                        //add message to list
                        sendMessage(mssgText.getText().toString());
                    }
                    handled = true;
                }
                return handled;
            }
        });

        //event for button SEND
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Hea too");
                if (mssgText.getText().toString().trim().equals("")) {
                    // TODO: disable button
                    Toast.makeText(ChatActivity2.this, "Please input some text...", Toast.LENGTH_SHORT).show();
                } else {
                    //add message to list
                    sendMessage(mssgText.getText().toString());
                }
            }
        });
    }

    private void initUI(){
        Log.e(TAG, SUB_TAG+"Initializig the ui");
        recyclerView = findViewById(R.id.list_msg);
        btnSend = findViewById(R.id.btn_chat_send);
        mssgText = findViewById(R.id.msg_type);

        initializeAdapter();
    }

    private void initializeAdapter(){
        Log.e(TAG, SUB_TAG+"initializing adapter");
        chatMessages = new ArrayList<ChatMessage>();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                chatMessages = new ArrayList<ChatMessage>(localDatabaseReference.getLastTwentyMessagesForSpecificUser(myChatPartner));
                //set ListView adapter first
                adapter = new OneOnOneMessageAdapter(chatMessages);
                recyclerView.setAdapter(adapter);
                layoutManager = new LinearLayoutManager(mActivity);
                recyclerView.setLayoutManager(layoutManager);
            }
        });
    }

    private void sendMessage(String mssg){
        Log.e(TAG, SUB_TAG+"sending message");

        long time  = Calendar.getInstance().getTimeInMillis();
        ChatMessage chatMessage = null;

        //TODO: this if was used for debugging and testing different types of mssges being sent,
        //  during the times we were testing with radio stuff, gotta decide on whether to keep this or remove it
        //  (most likely remove it)
        if(!(mssg.equals("4"))) {
            chatMessage = new ChatMessage(MainActivity.currentUser.getUid() + String.valueOf(time), mssg,
                    myChatPartner.getUid(), myChatPartner.getName(),
                    MainActivity.currentUser.getUid(), MainActivity.currentUser.getName(),
                    MainActivity.currentUser.latitude, MainActivity.currentUser.longitude,
                    time, true, ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());
        }

        pushMessageToFirebase(chatMessage);
        saveMessageLocally(chatMessage);

        //TODO: gotta add a way to send stuff thru the radio
//        sendMessageThruRadio(chatMessage);
    }

    private void pushMessageToFirebase(ChatMessage chatMessage){  
        Log.e(TAG, SUB_TAG+"Push message to firebase");
        String oneOnOneChatId = "";

        String firstUser = MainActivity.currentUser.getUid();
        String secondUser = myChatPartner.getUid();

        Map<String, Object> new_chat_mssg = chatMessage.toMap();

        Map<String, Object> firebase_child_update = new HashMap<>();

        if(getOneOnOneChatFirebaseID(firstUser, secondUser)){
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

        //Do the actual writing of the data onto firebase
        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);
    }

    private void saveMessageLocally(final ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Saving message locally");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                localDatabaseReference.saveChatMessageLocally(chatMessage);
            }
        });
    }

    private void addMessageOnScreen(final ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Adding the mssg on screen: " + mssg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatMessages.add(mssg);
                adapter.notifyDataSetChanged();
                mssgText.setText("");
            }
        });
    }

    //This message creates a unique id which will be used to identify the chat between the 2 users
    //  to whom the 2 ID's belong to
    private boolean getOneOnOneChatFirebaseID(String user1ID, String user2ID){
        Log.e(TAG, SUB_TAG+"getting the ID to be used on firebase");
        if((user2ID.compareTo(user1ID)) > 0){
            Log.e(TAG, SUB_TAG+"This is the id: " + user1ID+user2ID);
            return true;
        }
        Log.e(TAG, SUB_TAG+"This is the id: " + user2ID+user1ID);
        return false;
    }

    private void sendMessageThruRadio(ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Sending message thru radio.");
        String mssgForRadio = "";
        int mssgType = 0;
        if(chatMessage != null) {
            mssgForRadio = convertMessageToString(chatMessage);
            mssgType = chatMessage.mssgType;
        }
        else{
            mssgType = ChatMessage.ChatTypes.GETMESSAGESFROMRADIO.getValue();
        }
        Log.e(TAG, SUB_TAG+mssgForRadio.getBytes());
        BlueTerm.sendMessage(mssgForRadio, mssgType);
    }

    private String convertMessageToString(ChatMessage mssg){
        if(mssg == null)
            return null;
        String mssgStr = "{" +
                                "mssgId: " + mssg.mssgId + ","
                            +   "mssgText: " + mssg.mssgText + ","
                            +   "receiverId: " + mssg.receiverId + ","
                            +   "receiverName: " + mssg.receiverName + ","
                            +   "senderId: " + mssg.senderId + ","
                            +   "senderName: " + mssg.senderName + ","
                            +   "latitude: " + String.valueOf(mssg.latitude) + ","
                            +   "longtidue: " + String.valueOf(mssg.longitude) + ","
                            +   "time: " + String.valueOf(mssg.time) + ","
                            +   "isMyMssg: " + String.valueOf(mssg.isMyMssg) + ","
                            +   "mssgType: " + String.valueOf(mssg.mssgType)
                        + "}";
        return mssgStr;
    }

    @Override
    public void eventTriggered(HashMap<String, Object> packet, String type) {
        Log.e(TAG, SUB_TAG+"Event is triggered, with type: " + type);
        if(type.equals(Event.chatMssgEventID)) {
            Log.e(TAG, SUB_TAG+"this is a chat message event");
            ChatMessage mssg = MessageUtility.convertHashMapToChatMessage(packet);
            addMessageOnScreen(mssg);
        }
    }
}