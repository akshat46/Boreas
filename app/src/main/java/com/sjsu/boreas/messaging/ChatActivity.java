package com.sjsu.boreas.messaging;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.connection_handlers.NearbyConnectionHandler;
import com.sjsu.boreas.messages.TextMessage;

import java.util.List;

/**
 * Represents main chat activity. Pass in whichever set of messages or chat algorithm/participants
 * and this activity will work with any of them.
 * Lots of code from: https://www.scaledrone.com/blog/android-chat-tutorial/
 */
public class ChatActivity extends AppCompatActivity {

    private ChatMessageAdapter messageAdapter;
    private ListView textArea;

    private Button advertise, discover, broadcast;
    private EditText messageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        messageAdapter = new ChatMessageAdapter(this);
        textArea = (ListView) findViewById(R.id.chat_textarea);
        textArea.setAdapter(messageAdapter);

        addMessage(true, "Me", "Hello, world!");
        //addMessage(false, "Me", "Hello, app!");

        advertise = (Button) findViewById(R.id.chat_button_bluetooth);
        discover = (Button) findViewById(R.id.chat_button_radio);
        broadcast = (Button) findViewById(R.id.chat_button_wifi);

        messageText = (EditText) findViewById(R.id.chat_messagetext);
    }

    public void addMessage(boolean isSelf, String sender, String text){

        final Message message = new Message(text, sender, isSelf);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.addMessage(message);
                textArea.setSelection(textArea.getCount() - 1);
            }
        });
    }

    public void toggleBluetooth(View v){

    }

    public void toggleRadio(View v){

    }

    public void toggleWifi(View v){
        //messageAlgorithm.startBroadcast();
    }

    @Override
    protected void onStart(){
        super.onStart();
        MainActivity.nearbyConnectionHandler.setActiveActivity(this);
        List<TextMessage> messageList = MainActivity.nearbyConnectionHandler.dequeueGroupChats();
        for(TextMessage mess : messageList){
            addMessage(false, mess.sender.name, mess.message);
        }
    }

    @Override
    protected void onStop(){
        MainActivity.nearbyConnectionHandler.removeActiveActivity();
        super.onStop();
    }

    public void sendMessage(View v){
        String msg = messageText.getText().toString();
        if(msg.equals(""))
            return;

        MainActivity.nearbyConnectionHandler.sendGroupMessage(msg);
        messageText.setText("");
    }


}
