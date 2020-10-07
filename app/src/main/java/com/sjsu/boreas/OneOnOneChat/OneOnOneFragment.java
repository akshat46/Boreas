package com.sjsu.boreas.OneOnOneChat;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.ChatViewRelatedStuff.ChatActivity2;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.PotentialContacts.PotentialContacts;
import com.sjsu.boreas.Events.Event;
import com.sjsu.boreas.Events.EventListener;
import com.sjsu.boreas.LandingPage;
import com.sjsu.boreas.Database.Messages.MessageUtility;
import com.sjsu.boreas.R;
import com.sjsu.boreas.UserRecyclerViewStuff.UserListAdapter;
import com.sjsu.boreas.UserRecyclerViewStuff.UserListItemClickAction;
import com.sjsu.boreas.Database.AppDatabase;
import com.sjsu.boreas.Database.Contacts.User;

import java.util.ArrayList;
import java.util.HashMap;

public class OneOnOneFragment extends Fragment implements EventListener, UserListItemClickAction {
    public static final String EXTRA_TAB_NAME = "tab_name";
    private String mTabName;
    private RecyclerView recyclerView;
    private UserListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LandingPage mParent;
    private View rootView;
    private Context mContext;
    public static AppDatabase database;
    private ArrayList<User> contactArrayList;
    private UserListItemClickAction userListItemClickAction = this;
    private LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---OneOnOne___Frag ";

    public OneOnOneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Event event = Event.get("chatmessages");
        event.addListener(this);
        Log.e(TAG, SUB_TAG+"On create");
        super.onCreate(savedInstanceState);
        mParent = (LandingPage) getActivity();
    }

    public static OneOnOneFragment newInstance(String tabName) {
        Log.e(TAG, SUB_TAG+"OneOnOneFragment");
        Bundle args = new Bundle();
        args.putString(EXTRA_TAB_NAME, tabName);
        OneOnOneFragment fragment = new OneOnOneFragment();
        fragment.setArguments(args);
        return fragment;
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
        rootView = inflater.inflate(R.layout.fragment_one_on_one, container, false);
        mTabName = getArguments().getString(EXTRA_TAB_NAME);
        mContext = container.getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        Log.e(TAG, SUB_TAG+"initUI");
        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mParent);
        recyclerView.setLayoutManager(layoutManager);

        initializeAdapter();

        recyclerView.setAdapter(mAdapter);
    }

    private void initializeAdapter() {
        super.onStart();
        Log.e(TAG, SUB_TAG+"---- custom local list adapter");

        final LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                contactArrayList = new ArrayList<User>(localDatabaseReference.getContacts());
                //Also add any potential contacts chats
                contactArrayList.addAll(localDatabaseReference.getPotentialContacts());
                mAdapter=new UserListAdapter(contactArrayList, mContext, userListItemClickAction);
                recyclerView.setAdapter(mAdapter);
            }
        });
    }

    private void manageMessage(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Managing what to do with the received mssg");

        User user = new User(mssg.senderId, mssg.senderName, mssg.latitude, mssg.longitude);

        //First check if the user is even in our contacts or not?
        if(localDatabaseReference.isUserAlreadyInContacts(user)){
            Log.e(TAG, SUB_TAG+"The user is already in contacts, so start that management of stuff here");
            sendMssgToCorrectChatInstance(mssg, user);
        }
        else{
            PotentialContacts potentialContact = new PotentialContacts(user.uid, user.name, user.latitude, user.longitude);
            localDatabaseReference.addPotentialContact(potentialContact);
            Log.e(TAG, SUB_TAG+"The sender of the mssg: " + mssg.mssgText + ", is not in the contacts");
            sendMssgToCorrectChatInstance(mssg, potentialContact);
        }
    }

    private void addItemToOneOnOneListAdapter(User user){
        Log.e(TAG, SUB_TAG+"Adding an item to the adapter list: " + user);

        contactArrayList.add(user);
        mParent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void sendMssgToCorrectChatInstance(ChatMessage mssg, User user){
        Log.e(TAG, SUB_TAG+"Send mssg to the correct chat instance");

        int userIndex = indexOfUserInContactArray(user);
        //First check if a chat instance is already open
        if(userIndex >= 0){
            Log.e(TAG, SUB_TAG+"user is already in the adapter list");
            //Update the that list element here
        }
        else{
            Log.e(TAG, SUB_TAG+"Add a new list element for this user");
            addItemToOneOnOneListAdapter(user);
        }
    }

    private int indexOfUserInContactArray(User user){
        Log.e(TAG, SUB_TAG+"Finding the index of the given user/chat instance");

        for(int i = 0; i < contactArrayList.size(); i++){
            if(contactArrayList.get(i).getUid().equals(user.getUid())){
                Log.e(TAG, SUB_TAG+"The given user is found in the arraylist");
                return i;
            }
        }

        return -1;
    }

//    public void initializeFirebaseAdapter(){
//        Log.e(TAG, SUB_TAG+"InitializeFirebase adapter");
//
//        Query query = FirebaseDataRefAndInstance.getDatabaseReference().child("contacts").child(MainActivity.currentUser.getUid());
//
//        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
//                .setQuery(query, new SnapshotParser<User>() {
//                    @NonNull
//                    @Override
//                    public User parseSnapshot(@NonNull DataSnapshot snapshot) {
//                        Log.e(TAG, SUB_TAG+"Parse snapshot");
//                        return new User(snapshot.child("uid").getValue().toString(),
//                                snapshot.child("name").getValue().toString(),
//                                Double.parseDouble(snapshot.child("latitude").getValue().toString()),
//                                Double.parseDouble(snapshot.child("longitude").getValue().toString()),
//                                false);
//                    }
//                })
//                .build();
//
//        mAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
//            @NonNull
//            @Override
//            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                Log.e(TAG, SUB_TAG+"onCreateViewHolder");
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.item_chat, parent, false);
//
//                return new UsersViewHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull final User model) {
//                Log.e(TAG, SUB_TAG+"onBindViewHolder");
//                holder.bindToListItemView(model);
//
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Launch PostDetailActivity
//                        Intent intent = new Intent(getActivity(), ChatActivity2.class);
//                        //Passing the user object using intent
//                        intent.putExtra("ReceiverObj", model);
//                        startActivity(intent);
//                    }
//                });
//            }
//        };
//    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        mAdapter.startListening();
//    }

    @Override
    public void onStop() {
        super.onStop();
//        mAdapter.stopListening();
    }

    public void eventTriggered(HashMap<String, Object> packet){
        Log.e(TAG, SUB_TAG+"I have received a message, my lord. It goes something like: "+ packet.toString());
        // parse chatmessage from packet
        // try catch for parsing user when new user (not in contact list) messages
        ChatMessage mssg = MessageUtility.convertHashMapToChatMessage(packet);

        manageMessage(mssg);
    }

    //The UserListItemClick implemented function
    public void onItemClicked(User model) {
        Log.e(TAG, SUB_TAG+"on item clicked");
        Intent intent = new Intent(getActivity(), ChatActivity2.class);
        //Passing the user object using intent
        intent.putExtra("ReceiverObj", model);
        startActivity(intent);
    }
}
