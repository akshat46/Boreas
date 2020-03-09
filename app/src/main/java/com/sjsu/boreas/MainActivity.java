package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sjsu.boreas.connection_handlers.NearbyConnectionHandler;
import com.sjsu.boreas.database.AppDatabase;
import com.sjsu.boreas.database.User;
import com.sjsu.boreas.messaging.ChatActivity;
import com.sjsu.boreas.wifidirecttest.WDTestActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REGISTER_ACTIVTY_REQUEST_CODE = 0;
    private static final int GROUPCHAT_ACTIVTY_REQUEST_CODE = 1;
    private static final int FRIENDS_ACTIVTY_REQUEST_CODE = 2;
    private static final int EMERGENCY_ACTIVTY_REQUEST_CODE = 3;

    public static AppDatabase database;
    public static User currentUser;
    public static NearbyConnectionHandler nearbyConnectionHandler;

    private Button buttonRegister, buttonGroupchat, buttonFriends, buttonEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "mydatabase").build();


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
        ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        requestPermissions();

        //Connect the buttons in code to the UI buttons
        buttonRegister = (Button)findViewById(R.id.mainmenu_button_register);
        buttonGroupchat = (Button)findViewById(R.id.mainmenu_button_groupchat);
        buttonFriends = (Button)findViewById(R.id.mainmenu_button_friends);
        buttonEmergency = (Button)findViewById(R.id.mainmenu_button_emergency);

        //Start with buttons set to false
        buttonRegister.setEnabled(false);
        buttonGroupchat.setEnabled(false);
        buttonFriends.setEnabled(false);
        buttonEmergency.setEnabled(false);

        //Check if device is already registered
        checkRegistration();
    }

    /**
     * Checks if the user has registered yet and enable/disable buttons based on that
     * @return Whether user has registered yet
     */
    private void checkRegistration(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<User> mes = database.userDao().getMe();
                final int userSize = mes.size();
                System.out.println("Users registered as me: "+userSize);
                if(userSize > 0)
                    currentUser = mes.get(0);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enableBasedOnRegistration(userSize > 0);
                    }
                });

            }
        });
    }

    public void enableBasedOnRegistration(boolean isRegistered){
        buttonRegister.setEnabled(!isRegistered);
        buttonGroupchat.setEnabled(isRegistered);
        buttonFriends.setEnabled(isRegistered);
        buttonEmergency.setEnabled(isRegistered);
        if(!isRegistered)
            openRegistration(null);
        else{
            if(nearbyConnectionHandler == null)
                nearbyConnectionHandler = new NearbyConnectionHandler(this);
        }
    }

    public void openRegistration(View v){
//        Intent intent = new Intent(this, RegisterActivity.class);
//        startActivityForResult(intent, REGISTER_ACTIVTY_REQUEST_CODE);
        Intent i = new Intent(this, LandingPage.class);
        startActivity(i);
    }

    public void openGroupchat(View v){
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    public void openFriends(View v){

    }

    public void openEmergency(View v){

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(resultCode){
            case REGISTER_ACTIVTY_REQUEST_CODE:
                checkRegistration();
                break;
        }
    }

    //This is an anonymous class for the button actions listeners
    /*
    private View.OnClickListener buttonClickListener = new View.OnClickListener(){
        public void onClick(View v){
            Intent intent;
            switch (v.getId()){
                case R.id.bluetooth:
                    intent = new Intent(MainActivity.this, ChatActivity.class);
                    startActivity(intent);
                    break;
                case R.id.wifi:
                    intent = new Intent(MainActivity.this, WDTestActivity.class);
                    startActivity(intent);
                    break;
                case R.id.radio:
                    intent = new Intent(MainActivity.this, RegisterActivity.class);
                    //intent.setType()
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };*/

    public void requestPermissions(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            System.out.println("Permission not granted!");
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE,
                            Manifest.permission.CHANGE_NETWORK_STATE,
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                    },
                    15);
        }else{
            System.out.println("Permission already granted");
        }
    }
}
