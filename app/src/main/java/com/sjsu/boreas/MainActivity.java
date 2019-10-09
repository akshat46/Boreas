package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG";
    public static final String USER_DATA_FILE = "userdata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if device is already registered
        String [] files = fileList();

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

        /*
        File dir = getFilesDir();
        Log.v(TAG, "file directory: "+dir.getParent());
        Log.v(TAG, "File List: ");
        for(String myfile : fileList())
            Log.v(TAG, myfile);
        */
    }
}
