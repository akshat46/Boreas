package com.sjsu.boreas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;

public class SettingsActivity extends AppCompatActivity {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------Settings activity-- ";

    //private LoggedInUser currentUser = null;
    private TextView userNameLabel;
    private ImageView iv_edit;
    private Button logoutButton;
    private LocalDatabaseReference localDatabaseReference;
    private TextView userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Log.e(TAG, SUB_TAG + "on create");

       /* Intent intent = getIntent();
        currentUser = (LoggedInUser) intent.getSerializableExtra("currentUser");*/

        localDatabaseReference = LocalDatabaseReference.get();
        initView();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        localDatabaseReference = LocalDatabaseReference.get();
        initView();
    }

    private void initView() {
        Log.e(TAG, SUB_TAG + "Initializing view");
        userNameLabel = findViewById(R.id.settings_user_name);
        logoutButton = findViewById(R.id.logout_button);
        userToken = findViewById(R.id.user_token);
        iv_edit = findViewById(R.id.iv_edit);

        userNameLabel.setText(LandingPage.currentUser.name);
        userToken.setText(LandingPage.currentUser.getUid());

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG + "onclick logout");
                logout();
            }
        });

        iv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
               //intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });
    }

    private void logout() {
        Log.e(TAG, SUB_TAG + "loging out");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                localDatabaseReference.logUserOut(LandingPage.currentUser);
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