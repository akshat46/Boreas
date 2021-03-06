package com.sjsu.boreas.AddContacts;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseController;
import com.sjsu.boreas.ContactRecyclerItems.UserListAdapter;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.ContactRecyclerItems.UserListItemClickAction;
import com.sjsu.boreas.ContactRecyclerItems.UserViewHolder;
import com.sjsu.boreas.Database.Contacts.User;

import java.util.ArrayList;

public class OnlineListOfPeopleFragment extends Fragment implements UserListItemClickAction {
    private static final String EXTRA_TAB_NAME = "tab_name";
    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---Online list of people ---";

    private View rootView;
    private RecyclerView recyclerView;
    private ArrayList<User> userArrayList;
    private FirebaseRecyclerAdapter<User, UserViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private AddContactActivity mParent;
    private SearchView searchBar;
    private UserListAdapter mAdapter2;
    private Context mContext;
    private UserListItemClickAction userListItemClickAction = this;
    private LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"on create");
        super.onCreate(savedInstanceState);
        mParent = (AddContactActivity) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, SUB_TAG+"onAttach");
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"onCreateView");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_online_people_list, container, false);
        mContext = container.getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    public static OnlineListOfPeopleFragment newInstance(String tabName) {
        Log.e(TAG, SUB_TAG+"OneOnOneFragment");
        Bundle args = new Bundle();
        args.putString(EXTRA_TAB_NAME, tabName);
        OnlineListOfPeopleFragment fragment = new OnlineListOfPeopleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void initUI() {
        Log.e(TAG, SUB_TAG+"initUI");
        recyclerView = rootView.findViewById(R.id.online_ppl_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mParent);
        recyclerView.setLayoutManager(layoutManager);
        searchBar = rootView.findViewById(R.id.search_bar_online);

        initializeAdapter();

        manageMessageFromWorkerThread();
//        firebaseContactsAdapter();
    }

    private void searchOnlinePeoplList(String searchedName){
        Log.e(TAG, SUB_TAG+"    Search Firebase contacts");
        ArrayList<User> filteredContacts = new ArrayList<User>();

        for(User u : userArrayList){
            if(u.name.toLowerCase().contains(searchedName.toLowerCase())){
                filteredContacts.add(u);
            }
        }

        UserListAdapter newAdapter = new UserListAdapter(filteredContacts, mContext, userListItemClickAction);
        recyclerView.setAdapter(newAdapter);
    }

    public void initializeAdapter() {
        super.onStart();
//        mAdapter2.startListening();
        Log.e(TAG, SUB_TAG+"----intialize custom firebase");
        final DatabaseReference nm = FirebaseDatabase.getInstance().getReference().child("users");
        nm.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    userArrayList = new ArrayList<User>();
                    for (DataSnapshot npsnapshot : dataSnapshot.getChildren()){
                        Log.e(TAG, SUB_TAG+"    onstart new adapter snapshot thing");
                        final User u = new User(npsnapshot.child("uid").getValue().toString(),
                                npsnapshot.child("name").getValue().toString(),
                                Double.parseDouble(npsnapshot.child("latitude").getValue().toString()),
                                Double.parseDouble(npsnapshot.child("longitude").getValue().toString()),
                                npsnapshot.child("publicKey").getValue().toString());
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                if(MainActivity.currentUser.uid != u.uid && !localDatabaseReference.isUserAlreadyInContacts(u))
                                    userArrayList.add(u);
                            }
                        });
                    }
                    mAdapter2=new UserListAdapter(userArrayList, mContext, userListItemClickAction);
                    recyclerView.setAdapter(mAdapter2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(searchBar != null){
            searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.e(TAG, SUB_TAG+"    searching firebase contact");
                    searchOnlinePeoplList(newText);
                    return true;
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        mAdapter2.stopListening();
    }

    private void manageMessageFromWorkerThread(){
        Log.e(TAG, SUB_TAG+"***********************Manage messg from worker thread");

    }

    @Override
    public void onItemClicked(User model, int position) {
        Log.e(TAG, SUB_TAG+"on item clicked");
        FirebaseController.addContact(model);
        localDatabaseReference.addContact(model);
        markAddedContact(position);
    }

    private void markAddedContact(int position){
        Log.e(TAG, SUB_TAG+"Marking the added contact");
        View view = layoutManager.findViewByPosition(position);
        ImageView added = (ImageView) view.findViewById(R.id.added);
        added.setVisibility(View.VISIBLE);
        mAdapter2.notifyItemChanged(position);
    }
}
