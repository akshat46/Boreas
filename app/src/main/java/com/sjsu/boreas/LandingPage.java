package com.sjsu.boreas;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.sjsu.boreas.LandingPageTabAdapterStuff.ViewPagerTabAdapter;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.GroupChats.OfflineGroupFragment;
import com.sjsu.boreas.OneOnOneChat.OneOnOneFragment;
import com.sjsu.boreas.Database.Contacts.User;

import java.util.ArrayList;
import java.util.List;

public class LandingPage extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    private static String TAG = "Boreas";
    private static String SUB_TAG = "-----Landing Page";
    private NavigationView mNavigationView;
    private FragmentTransaction mFragmentTransaction;
    private LoggedInUser currentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"In landing");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

		//~ setUpNavigationDrawer();

        Intent intent = getIntent();
        currentUser = (LoggedInUser) intent.getSerializableExtra("currentUser");
		
		init();
		makeADummyUserForFirebase();

    }

    private void makeADummyUserForFirebase(){
        Log.e(TAG, SUB_TAG+"makeADummyUserForFirebase");
        User u = new User("23", "name of", 123.4, -123.4);
    }

    private void init() {
		Log.e(TAG, SUB_TAG+"Init");
        initViews();
    }

    private void initViews() {
		Log.e(TAG, SUB_TAG+"InitViews");
        initToolbar();
        initAddContactFloatingButton();
        initSettingsFloatingButtons();
        initViewPager();
        initTabLayout();
        if(MainActivity.newAcct)
            showTokenDialogBox();
    }

    private void showTokenDialogBox(){
        Log.e(TAG, SUB_TAG+"Showing the token dialog box");
        MainActivity.newAcct = false;
        Log.e(TAG, SUB_TAG+"new acct got created");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Your USER Id token is: " + currentUser.getUid() +
                "\nPlease save this in a secure location, you need this to access your account in-case you get logged out.");
        dialog.setTitle("User ID");
        dialog.setCancelable(true);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, SUB_TAG+"Clicked the ok button in dialog box");
                dialog.dismiss();
            }
        });

        dialog.create().show();
    }

    private void initToolbar() {
		Log.e(TAG, SUB_TAG+"InitToolbar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initAddContactFloatingButton() {
		Log.e(TAG, SUB_TAG+"InitNewMessageFloatingButton");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, SUB_TAG+"onClick of initNewMessageFloatingButton");
                Intent intent = new Intent(view.getContext(), AddContactActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initSettingsFloatingButtons(){
        Log.e(TAG, SUB_TAG+"Initializing settings floating button");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.settings_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"On click settings button");
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                intent.putExtra("currentUser", currentUser);
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
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_contacts_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_location_city_white_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_cloud_white_24dp);
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
        }

        return super.onOptionsItemSelected(item);
    }


    private List<Fragment> mFragments;

    private List<Fragment> getFragments() {

        mFragments = new ArrayList<Fragment>();
        mFragments.add(OneOnOneFragment.newInstance(""));
        mFragments.add(OfflineGroupFragment.newInstance(""));
        mFragments.add(OneOnOneFragment.newInstance(""));

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
