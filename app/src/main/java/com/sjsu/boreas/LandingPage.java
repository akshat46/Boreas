package com.sjsu.boreas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.sjsu.boreas.ChatViewRelatedStuff.ViewPagerTabAdapter;
import com.sjsu.boreas.ViewFragments.OfflineGroupFragment;
import com.sjsu.boreas.ViewFragments.OneOnOneFragment;

import java.util.ArrayList;
import java.util.List;

public class LandingPage extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static String TAG = "Boreas";
    private static String SUB_TAG = "-----Landing Page";
    private NavigationView mNavigationView;
    private FragmentTransaction mFragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG + "In landing");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        //~ setUpNavigationDrawer();

        init();

    }

    private void init() {
        Log.e(TAG, SUB_TAG + "Init");
        initViews();
    }

    private void initViews() {
        Log.e(TAG, SUB_TAG + "InitViews");
        initToolbar();
        initNewMessageFloatingButton();
        initViewPager();
        initTabLayout();
    }

    private void initToolbar() {
        Log.e(TAG, SUB_TAG + "InitToolbar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initNewMessageFloatingButton() {
        Log.e(TAG, SUB_TAG + "InitNewMessageFloatingButton");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, SUB_TAG + "onClick of initNewMessageFloatingButton");
                Intent intent = new Intent(LandingPage.this, DeviceSearchActivity.class);
                startActivity(intent);
            }
        });
    }


    private ViewPager mViewPager;

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        List<String> tabNames = new ArrayList<String>();
        tabNames.add("Friends");
        tabNames.add("Local");
        tabNames.add("Global");
        ViewPagerTabAdapter viewPagerTabAdapter = new ViewPagerTabAdapter(getSupportFragmentManager(), getFragments(), tabNames);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(viewPagerTabAdapter);
    }


    private void initTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        } else if (id== R.id.action_account){
            startActivity(new Intent(this, ProfileEditActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private List<Fragment> mFragments;

    private List<Fragment> getFragments() {

        mFragments = new ArrayList<Fragment>();
        mFragments.add(OneOnOneFragment.newInstance(""));
        mFragments.add(OfflineGroupFragment.newInstance(""));
        mFragments.add(OfflineGroupFragment.newInstance(""));

        return mFragments;
    }

//    private void setUpNavigationDrawer(){
//		final DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.navigation);
//
//
//        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//
//        //Set the default fragment (i think)
//        mFragmentTransaction.add(R.id.main_container, new OneOnOneFragment());
//        mFragmentTransaction.commit();
//
//        mNavigationView = findViewById(R.id.navigation);
//        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()){
//
//                    case R.id.help:
//                        Log.e(TAG, SUB_TAG+"Starting help");
//                        break;
//                    case R.id.offline_group_chat:
//                        Log.e(TAG, SUB_TAG+"Starting offline group chat fragment activity.");
//                        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//                        mFragmentTransaction.replace(R.id.main_container, new OfflineGroupFragment());
//                        mFragmentTransaction.commit();
//                        item.setChecked(true);
//                        drawer.closeDrawers();
//                        break;
//                    case R.id.online_group_chat:
//                        Log.e(TAG, SUB_TAG+"Starting online group chat fragment activity.");
//                        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//                        mFragmentTransaction.replace(R.id.main_container, new OnlineGroupFragment());
//                        mFragmentTransaction.commit();
//                        item.setChecked(true);
//                        drawer.closeDrawers();
//                        break;
//                    case R.id.single_mssg_chat:
//                        Log.e(TAG, SUB_TAG+"Starting one on one chat fragment activity.");
//                        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//                        mFragmentTransaction.replace(R.id.main_container, new OneOnOneFragment());
//                        mFragmentTransaction.commit();
//                        item.setChecked(true);
//                        drawer.closeDrawers();
//                        break;
//                }
//
//                return false;
//            }
//        });
//	}

}
