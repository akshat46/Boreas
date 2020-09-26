package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.sjsu.boreas.Database.Users.User;

public class SettingsActivity extends AppCompatActivity {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------Settings activity-- ";

    private User currentUser = null;
    private TextView userNameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.e(TAG, SUB_TAG+"on create");

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        initView();
    }

    private void initView(){
        Log.e(TAG, SUB_TAG+"Initializing view");
        userNameLabel = findViewById(R.id.settings_user_name);
        userNameLabel.setText(currentUser.name);
    }
}