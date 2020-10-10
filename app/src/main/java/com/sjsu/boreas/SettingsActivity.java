package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;

public class SettingsActivity extends AppCompatActivity {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------Settings activity-- ";

    private LoggedInUser currentUser = null;
    private TextView userNameLabel;
    private Button logoutButton;
    private LocalDatabaseReference localDatabaseReference;
    private TextView userToken;

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

        userNameLabel.setText(currentUser.name);
        userToken.setText(currentUser.getUid());

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"onclick logout");
                logout();
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
}