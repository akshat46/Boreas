package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseDataRefAndInstance;
import com.sjsu.boreas.SecurityRelatedStuff.SecurityStuff;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "--------LoginActivity--- ";

    private EditText userID;
    private EditText password;
    private Button loginButton;
    private Button signupButton;

    private LocalDatabaseReference localDatabaseReference;

    private LoggedInUser loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.e(TAG, SUB_TAG+"onCreate");

        localDatabaseReference = LocalDatabaseReference.get();
        loggedInUser = null;

        userID = findViewById(R.id.login_userid);
        password = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.sign_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"signup button on click listener");
                tryLoggingIn();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"clicking on the sign up button");
                MainActivity.context.onActivityResult(0, MainActivity.REGISTER_ACTIVTY_START_CODE, null);
            }
        });
    }

    private void tryLoggingIn(){
        Log.e(TAG, SUB_TAG+"Trying to login");

        String passwordStr = password.getText().toString();
        String userIDStr = userID.getText().toString();

        if(passwordStr.equals("") || userIDStr.equals("")){
            Log.e(TAG, SUB_TAG+"One or both of the fields are empty");
            return;
        }

        String hashedPassword = SecurityStuff.hashThePassword(passwordStr);

        checkLocalDatabase(userIDStr, hashedPassword);
    }

    private void checkLocalDatabase(final String userID, final String password){
        Log.e(TAG, SUB_TAG+"checking local database for the user");
        final User user = null;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                localDatabaseReference.logUserIn(userID, password);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(loggedInUser != null)
                            success();
                        else
                            checkFirebase(userID, password);
                    }
                });
            }
        });
    }

    private void checkFirebase(String userID, String password){
        Log.e(TAG, SUB_TAG+"Checking firebase");
        loggedInUser = FirebaseDataRefAndInstance.checkLogInInfo(userID, password, this);
        if(loggedInUser != null) {
            //TODO: gotta replace the currently registered user with this new user from Firebase
            saveLoggedInUserLocally();
            success();
        }
        failed();
    }

    private void saveLoggedInUserLocally(){
        Log.e(TAG, SUB_TAG+"Save the user gotten from firebase locally as well");
        localDatabaseReference.registerUser(loggedInUser);
    }

    private void success(){
        Log.e(TAG, SUB_TAG+"success");
        localDatabaseReference.wipeAllPreviousUserData();
        MainActivity.context.checkRegistration();
    }

    private void failed(){
        Log.e(TAG, SUB_TAG+"Login failed, please review ur credentials, or try again later.");
    }

}