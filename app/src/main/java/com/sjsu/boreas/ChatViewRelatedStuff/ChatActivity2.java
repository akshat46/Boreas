package com.sjsu.boreas.ChatViewRelatedStuff;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sjsu.boreas.Firebase.FirebaseDataRefAndInstance;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.database.Messages.ChatMessage;
import com.sjsu.boreas.database.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity2 extends AppCompatActivity {

    private ListView listView;
    private View btnSend;
    private EditText mssgText;
    boolean myMessage = true;
    private List<ChatBubble> chatBubbles;
    private ArrayAdapter<ChatBubble> adapter;
    private User myChatPartner;
    private List<ChatMessage> mssgList;

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "----------------ChatActivity2 ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        Log.e(TAG, SUB_TAG+"We in hea on create");
        //Getting the user object from intent
        Intent intent = getIntent();
        myChatPartner = (User) intent.getSerializableExtra("ReceiverObj");

        Log.e(TAG, SUB_TAG+"-=-=-=-=-==-///////// "+myChatPartner);

        initializeChatScreen();

        listView = findViewById(R.id.list_msg);
        btnSend = findViewById(R.id.btn_chat_send);
        mssgText = findViewById(R.id.msg_type);

        //set ListView adapter first
        adapter = new MessageAdapter(this, R.layout.left_chat_bubble, chatBubbles);
        listView.setAdapter(adapter);

        //event for button SEND
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Hea too");
                if (mssgText.getText().toString().trim().equals("")) {
                    Toast.makeText(ChatActivity2.this, "Please input some text...", Toast.LENGTH_SHORT).show();
                } else {
                    //add message to list
                    sendMessage(mssgText.getText().toString());
                }
            }
        });
    }

    private void initializeChatScreen(){
        Log.e(TAG, SUB_TAG+"initialize Chat Screen");
        chatBubbles = new ArrayList<>();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mssgList = MainActivity.database.chatMessageDao().getLastTwentyMessagesForUser(myChatPartner.getUid(),
                                        ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());
                if(!(mssgList.isEmpty())){
                    for(int i = 0; i < mssgList.size(); i++){
                        chatBubbles.add(new ChatBubble(mssgList.get(i).mssgText, mssgList.get(i).isMyMssg));
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void sendMessage(String mssg){
        Log.e(TAG, SUB_TAG+"sending message");

        long time  = Calendar.getInstance().getTimeInMillis();

        ChatMessage chatMessage = new ChatMessage(MainActivity.currentUser.getUid()+String.valueOf(time), mssg,
                                            myChatPartner.getUid(), myChatPartner.getName(),
                                            MainActivity.currentUser.getUid(), MainActivity.currentUser.getName(),
                                            MainActivity.currentUser.latitude, MainActivity.currentUser.longitude,
                                            time, true, ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());

        ChatBubble ChatBubble = new ChatBubble(mssgText.getText().toString(), myMessage);

        pushMessageToFirebase(chatMessage);
        saveMessageLocally(chatMessage);

        chatBubbles.add(ChatBubble);
        adapter.notifyDataSetChanged();
        mssgText.setText("");
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
                MainActivity.database.chatMessageDao().insertAll(chatMessage);
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

}