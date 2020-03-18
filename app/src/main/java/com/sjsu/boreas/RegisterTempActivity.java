package com.sjsu.boreas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RegisterTempActivity extends AppCompatActivity {

    private LinearLayout registerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_temp);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int margin = (width/6);
        registerLayout = (LinearLayout) findViewById(R.id.login_layout);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) registerLayout.getLayoutParams();
        params.setMargins(margin,0,0,0);
    }
}
