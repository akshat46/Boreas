package com.sjsu.boreas;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.sjsu.boreas.Database.DatabaseReference;
import com.sjsu.boreas.HelperStuff.ContextHelper;
import com.sjsu.boreas.UserRecyclerViewStuff.UserListAdapter;
import com.sjsu.boreas.ChatViewRelatedStuff.ViewPagerTabAdapter;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseDataRefAndInstance;
import com.sjsu.boreas.AddContacts.OfflinePeopleContactedListFragment;
import com.sjsu.boreas.AddContacts.OnlineListOfPeopleFragment;
import com.sjsu.boreas.UserRecyclerViewStuff.UsersViewHolder;
import com.sjsu.boreas.Database.Users.User;

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

    private ContextHelper contextHelper = ContextHelper.get(null);
    private DatabaseReference databaseReference = DatabaseReference.get(contextHelper.getApplicationContext());

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
        final Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/contacts/" + MainActivity.currentUser.getUid() + "/" + user.getUid(), new_user);
        Log.e(TAG, SUB_TAG+"My user ID is: " + MainActivity.currentUser.getUid() + ", and the contact id is: " + user.getUid());

        //Do the actual writing of the data onto firebase and locally
        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);

    }
}
