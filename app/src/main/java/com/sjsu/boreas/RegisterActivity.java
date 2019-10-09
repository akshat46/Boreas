package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileOutputStream;

public class RegisterActivity extends AppCompatActivity {

    String KEY = "AIzaSyDyGjh3NUYPVNdxlbRdZD38FDrX-bOf5B4";

    EditText fullNameEditor;
    EditText userIdEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullNameEditor = findViewById(R.id.regitem_name);
        userIdEditor = findViewById(R.id.regitem_idname);
        Button addLocationButton = (Button) findViewById(R.id.regitem_coordbutton);

    }

    /**
     * Called by add location button
     * @param view The location button itself
     */
    public void addLocation(View view){

    }

    /**
     * Called by REGISTER button
     * @param view The register button itself
     */
    public void completeRegistration(View view){
        String name = fullNameEditor.getText().toString();
        String id = userIdEditor.getText().toString();
        String toWrite = name + "\n" + id;
        FileOutputStream stream;
        try{
            stream = openFileOutput(MainActivity.USER_DATA_FILE, Context.MODE_PRIVATE);
            stream.write(toWrite.getBytes());
            stream.close();

            //Switch to chat activity
            Intent intent = new Intent(this, ChatMenuActivity.class);
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.reg_error, Toast.LENGTH_SHORT).show();
        }
    }
}
