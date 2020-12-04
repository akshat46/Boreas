package com.sjsu.boreas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.sjsu.boreas.AddContacts.AddContactActivity;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.Misc.AppBarButtonsHandler;
import com.sjsu.boreas.Misc.ContextHelper;
import com.sjsu.boreas.OneOnOneChat.NearbyListFragment;
import com.sjsu.boreas.OneOnOneChat.OneOnOneFragment;
import com.sjsu.boreas.Database.Contacts.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LandingPage extends FragmentActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static String TAG = "Boreas";
    private static String SUB_TAG = "-----Landing Page";
    private NavigationView mNavigationView;
    private FragmentTransaction mFragmentTransaction;
    private LoggedInUser currentUser = null;
    private AppBarButtonsHandler mbuttonsHandler = new AppBarButtonsHandler(0);
    private ViewPager2 mViewPager;
    private TextView fragmentTitle;
    private ImageView avatar;
    public ImageButton refreshButton;
    public ImageButton refreshError;
    public ProgressBar loading;
    CustomFragmentStateAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG + "In landing");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        fragmentTitle= findViewById(R.id.fragmentTitle);
        refreshButton = findViewById(R.id.refresh_button);
        refreshError = findViewById(R.id.error);
        loading = findViewById(R.id.loading);

        refreshError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG + "Don't click on error jackass");
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(ContextHelper.get().getApplicationContext(),R.color.background));
        }

        //~ setUpNavigationDrawer();

        Intent intent = getIntent();
        currentUser = (LoggedInUser) intent.getSerializableExtra("currentUser");
        Log.e(TAG, SUB_TAG + " COME GET YOUR PRIVATE KEY HERE: \n" + currentUser.privateKey);
        Log.e(TAG, SUB_TAG + " COME GET YOUR PUBLIC KEY HERE: \n" + currentUser.publicKey);

        initViews();
        makeADummyUserForFirebase();

    }

    private void makeADummyUserForFirebase() {
        Log.e(TAG, SUB_TAG + "makeADummyUserForFirebase");
        User u = new User("23", "name of", 123.4, -123.4, "");
    }

    private void initViews() {
        Log.e(TAG, SUB_TAG + "InitViews");
        initToolbar();
        initSettingsButtons();
        initViewPager();
        initAppBar();
        if (MainActivity.newAcct)
            showTokenDialogBox();
    }

    private void showTokenDialogBox() {
        Log.e(TAG, SUB_TAG + "Showing the token dialog box");
        MainActivity.newAcct = false;
        Log.e(TAG, SUB_TAG + "new acct got created");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Your USER Id token is: " + currentUser.getUid() +
                "\nPlease save this in a secure location, you need this to access your account in-case you get logged out.");
        dialog.setTitle("User ID");
        dialog.setCancelable(true);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, SUB_TAG + "Clicked the ok button in dialog box");
                dialog.dismiss();
            }
        });

        dialog.create().show();
    }

    private void initToolbar() {
        Log.e(TAG, SUB_TAG + "InitToolbar");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
    }

    private void initSettingsButtons() {
        Log.e(TAG, SUB_TAG + "Initializing settings floating button");
        ImageButton settings = (ImageButton) findViewById(R.id.settings_button);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG + "On click settings button");
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });
    }

    private void initViewPager() {
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setUserInputEnabled(false);
        mAdapter = new CustomFragmentStateAdapter(this);
        mViewPager.setAdapter(mAdapter);
    }

    private void initAppBar() {
        Log.e(TAG, SUB_TAG + " Initializing bottom app bar.");

        ArrayList<String> imageButtonIDs = new ArrayList<String>(
                Arrays.asList("bottombar_contacts", "bottombar_nearby"));
        ImageButton b;
        final String[] fragmentTitles = new String[]{"CHATS", "NEARBY"};

        Log.e(TAG, SUB_TAG + " Initializing bottom app bar: Buttons");
        for (int i = 0; i < imageButtonIDs.size(); i++) {
            b = findViewById(getResources().getIdentifier(imageButtonIDs.get(i), "id", "com.sjsu.boreas"));
            mbuttonsHandler.addButton(b);
            if (i == 0) b.setSelected(true);
            else b.setSelected(false);
            final int temp = i;
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(temp);
                    fragmentTitle.setText(fragmentTitles[temp]);
                    mbuttonsHandler.setState(temp);
                    if(temp==1) {
                        refreshButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        refreshButton.setVisibility(View.GONE);
                        refreshError.setVisibility(View.GONE);
                    }
                }
            });
        }

        Log.e(TAG, SUB_TAG + " Initializing bottom app bar: FAB");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, SUB_TAG + "onClick of initNewMessageFloatingButton");
                Intent intent = new Intent(view.getContext(), AddContactActivity.class);
                startActivity(intent);
            }
        });

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

    private static List<Fragment> getFragments() {
        List<Fragment> mFragments;
        mFragments = new ArrayList<Fragment>();
        mFragments.add(OneOnOneFragment.newInstance(""));
        mFragments.add(NearbyListFragment.newInstance(""));
        return mFragments;
    }

    private class CustomFragmentStateAdapter extends FragmentStateAdapter {
        private List<Fragment> mFragments;
        private List<String> mTitles;
        private int COUNT = 2;

        public CustomFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
            mFragments = LandingPage.getFragments();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position > mFragments.size() || position < 0) return null;
            return mFragments.get(position);
        }

        @Override
        public int getItemCount() {
            return COUNT;
        }
    }

}
