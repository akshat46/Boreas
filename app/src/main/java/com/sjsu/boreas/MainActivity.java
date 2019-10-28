package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sjsu.boreas.wifidirecttest.WDTestActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG";
    public static final String USER_DATA_FILE = "userdata";
    private Button bluetooth, wifi, radio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
        ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        //Connect the buttons in code to the UI buttons
        bluetooth = (Button)findViewById(R.id.bluetooth);
        wifi = (Button)findViewById(R.id.wifi);
        radio = (Button)findViewById(R.id.radio);

        //Set the action listeners for the buttons
        bluetooth.setOnClickListener(buttonClickListener);
        wifi.setOnClickListener(buttonClickListener);
        radio.setOnClickListener(buttonClickListener);

        //Check if device is already registered
        String [] files = fileList();

        /*
        //Register device
        if(files.length  == 0){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
        //Already registered, go to chat
        else{
            Intent intent = new Intent(this, ChatMenuActivity.class);
            startActivity(intent);
        }
        */
    }

    //This is an anonymous class for the button actions listeners
    private View.OnClickListener buttonClickListener = new View.OnClickListener(){
        public void onClick(View v){
            switch (v.getId()){
                case R.id.bluetooth:
                    break;
                case R.id.wifi:
                    Intent intent = new Intent(MainActivity.this, WDTestActivity.class);
                    startActivity(intent);
                    break;
                case R.id.radio:
                    break;
                default:
                    break;
            }
        }
    };
}
