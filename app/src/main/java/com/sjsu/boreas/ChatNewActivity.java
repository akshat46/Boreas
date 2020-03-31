package com.sjsu.boreas;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.sjsu.boreas.ViewFragments.ChatFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatNewActivity extends AppCompatActivity {

    public static final String EXTRAS_USER = "user";
    public static final String tag = ChatNewActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_new);
        init();
    }

    private void init() {
        setArguments();
        initView();
    }

    private void initView() {
        initToolbar();
        showFragment();
    }

    private void showFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, ChatFragment.newInstance(getIntent().getExtras()));
        ft.commit();
    }

    private void setArguments() {
        Bundle bundle = getIntent().getExtras();
        setTitle("Arjun");
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView nameTV = (TextView) findViewById(R.id.nameTV);
        nameTV.setText("Arjun");

        CircleImageView userIV = (CircleImageView) findViewById(R.id.userIV);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Toolbar", "Clicked");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

}
