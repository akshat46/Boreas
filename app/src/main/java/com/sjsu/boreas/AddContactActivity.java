package com.sjsu.boreas;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.sjsu.boreas.Adapters.UserListAdapter;
import com.sjsu.boreas.ChatViewRelatedStuff.ViewPagerTabAdapter;
import com.sjsu.boreas.online_connection_handlers.FirebaseDataRefAndInstance;
import com.sjsu.boreas.AddContacts.OfflinePeopleContactedListFragment;
import com.sjsu.boreas.AddContacts.OnlineListOfPeopleFragment;
import com.sjsu.boreas.ViewHolder.UsersViewHolder;
import com.sjsu.boreas.database.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddContactActivity extends AppCompatActivity {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------ADdContactActivity ";

    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private FirebaseRecyclerAdapter<User, UsersViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LandingPage mParent;
    private SearchView searchBar;
    private UserListAdapter mAdapter2;
    private ViewPager mViewPager;
    private FragmentTransaction mFragmentTransaction;
    private List<Fragment> mFragments;


//    private DatabaseReference database_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"on create");
        super.onCreate(savedInstanceState);
        userArrayList = new ArrayList<User>();
        setContentView(R.layout.activity_add_contact);
        initViews();
    }

    private void initViews() {
        Log.e(TAG, SUB_TAG+"InitViews");
        initViewPager();
        initTabLayout();
    }

    private void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        List<String> tabNames = new ArrayList<String>();
        tabNames.add("Online People List");
        tabNames.add("Offline People List");
        ViewPagerTabAdapter viewPagerTabAdapter = new ViewPagerTabAdapter(getSupportFragmentManager(), getFragments(), tabNames);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(viewPagerTabAdapter);
    }

    private void initTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_contacts_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_contacts_white_24dp);
    }

    private List<Fragment> getFragments() {

        mFragments = new ArrayList<Fragment>();
        mFragments.add(OnlineListOfPeopleFragment.newInstance(""));
        mFragments.add(OfflinePeopleContactedListFragment.newInstance(""));

        return mFragments;
    }

    public static void addContact(final User user){
        Log.e(TAG, SUB_TAG+"addContact");

        Map<String, Object> new_user = user.toMap();
        Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/contacts/" + MainActivity.currentUser.getUid() + "/" + user.getUid(), new_user);
        Log.e(TAG, SUB_TAG+"My user ID is: " + MainActivity.currentUser.getUid() + ", and the contact id is: " + user.getUid());

        //Do the actual writing of the data onto firebase and locally
        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);
        AsyncTask.execute(new Runnable() {
            public Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    // This is where you do your work in the UI thread.
                    // Your worker tells you in the message what to do.
                    Log.e(TAG, SUB_TAG+"&&&&&&&&&&&&&&&&&&&&&&: " + message.obj);
//                Toast.makeText(getApplicationContext(), (Integer) message.obj, Toast.LENGTH_LONG).show();
                }
            };

            @Override
            public void run() {
                Log.e(TAG, SUB_TAG+"Adding contact: " + user.getName());
                if(MainActivity.database.userDao().getSpecificUser(user.getUid()).isEmpty()) {
                    Log.e(TAG, SUB_TAG+"User doesn't already exist in the contacts");
                    MainActivity.database.userDao().insertAll(user);
                    Message mssg = new Message();
                    mssg.obj = "This user with name: " + user.getName() + ", has been added locally (SUCESS!!!!)";
                    handler.dispatchMessage(mssg);
                }
                else{
                    Log.e(TAG, SUB_TAG+">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+MainActivity.database.userDao().getUsers());
                    Message mssg = new Message();
                    mssg.obj = "This user with name: " + user.getName() + ", is already in the contacts (No success!!!!)";
                    handler.dispatchMessage(mssg);
                    Log.e(TAG, SUB_TAG+mssg.obj);
                }
            }
        });
    }
}
