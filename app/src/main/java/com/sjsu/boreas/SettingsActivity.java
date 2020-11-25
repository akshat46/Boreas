package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.Events.Event;
import com.sjsu.boreas.Events.EventListener;
import com.sjsu.boreas.PhoneBluetoothRadio.BlueTerm;

import java.util.HashMap;

import javax.security.auth.Subject;

public class SettingsActivity extends AppCompatActivity implements EventListener {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------Settings activity-- ";

    private LoggedInUser currentUser = null;
    private TextView userNameLabel;
    private Button logoutButton;
    private Button checkRadioButton;
    private LocalDatabaseReference localDatabaseReference;
    private TextView userToken;

    public static boolean radio_is_connected = false;

    private Event event_radio_connected = Event.get(Event.radioConnected);
    private Event event_radio_disconnected = Event.get(Event.radioDisconnected);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.e(TAG, SUB_TAG+"on create");

        Intent intent = getIntent();
        currentUser = (LoggedInUser) intent.getSerializableExtra("currentUser");

        localDatabaseReference = LocalDatabaseReference.get();

        initView();
    }

    private void initView(){
        Log.e(TAG, SUB_TAG+"Initializing view");
        userNameLabel = findViewById(R.id.settings_user_name);
        logoutButton = findViewById(R.id.logout_button);
        userToken = findViewById(R.id.user_token);
        checkRadioButton = findViewById(R.id.check_radio_button);

        userNameLabel.setText(currentUser.name);
        userToken.setText(currentUser.getUid());

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"onclick logout");
                logout();
            }
        });

        checkRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG + "Checking what happening with this radio tho");
                checkForRadioConnection();
            }
        });
    }

    private void logout(){
        Log.e(TAG, SUB_TAG+"loging out");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                localDatabaseReference.logUserOut(currentUser);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.context.checkRegistration();
                    }
                });
            }
        });
    }

    private void checkForRadioConnection(){
        Log.e(TAG, SUB_TAG+"Checking for that connect tho");
        boolean isRadioConnected = BlueTerm.getSerialConnectionStatus();

        if(isRadioConnected){
            Log.e(TAG, SUB_TAG + "Radio is connected");
            event_radio_connected.trigger(null);
        }
        else{
            Log.e(TAG, SUB_TAG+"Radio isn't connected yo");
            event_radio_disconnected.trigger(null);
        }
    }

    private void setCheckRadioButtonColor(final int color){
        Log.e(TAG, SUB_TAG + "Setting the button color to: " + color);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkRadioButton.setBackgroundColor(color);
            }
        });

    }

    @Override
    public void eventTriggered(HashMap<String, Object> packet, String type) {
        Log.e(TAG, SUB_TAG + "Event was triggered");

        if(type.equals(Event.radioConnected)){
            Log.e(TAG, SUB_TAG+"Radio device connected");
            radio_is_connected = true;
            setCheckRadioButtonColor(Color.GREEN);
        }
        else if(type.equals(Event.radioDisconnected)){
            Log.e(TAG, SUB_TAG+"Radio device disconnected");
            radio_is_connected = false;
            setCheckRadioButtonColor(Color.MAGENTA);
        }
    }
}