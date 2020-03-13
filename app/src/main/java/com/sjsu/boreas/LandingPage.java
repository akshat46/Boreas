package com.sjsu.boreas;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.navigation.NavigationView;
import com.sjsu.boreas.ViewFragments.OfflineGroupFragment;
import com.sjsu.boreas.ViewFragments.OneOnOneFragment;
import com.sjsu.boreas.ViewFragments.OnlineGroupFragment;

public class LandingPage extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    private static String TAG = "-----Landing Page-----";
    private NavigationView mNavigationView;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "In landing");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation);


        mFragmentTransaction = getSupportFragmentManager().beginTransaction();

        //Set the default fragment (i think)
        mFragmentTransaction.add(R.id.main_container, new OneOnOneFragment());
        mFragmentTransaction.commit();

        mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.help:
                        Log.e(TAG, "Starting help");
                        break;
                    case R.id.offline_group_chat:
                        Log.e(TAG, "Starting offline group chat fragment activity.");
                        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        mFragmentTransaction.replace(R.id.main_container, new OfflineGroupFragment());
                        mFragmentTransaction.commit();
                        item.setChecked(true);
                        drawer.closeDrawers();
                        break;
                    case R.id.online_group_chat:
                        Log.e(TAG, "Starting online group chat fragment activity.");
                        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        mFragmentTransaction.replace(R.id.main_container, new OnlineGroupFragment());
                        mFragmentTransaction.commit();
                        item.setChecked(true);
                        drawer.closeDrawers();
                        break;
                    case R.id.single_mssg_chat:
                        Log.e(TAG, "Starting one on one chat fragment activity.");
                        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        mFragmentTransaction.replace(R.id.main_container, new OneOnOneFragment());
                        mFragmentTransaction.commit();
                        item.setChecked(true);
                        drawer.closeDrawers();
                        break;
                }

                return false;
            }
        });

    }

}
