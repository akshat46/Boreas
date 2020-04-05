package com.sjsu.boreas;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sjsu.boreas.Firebase.FirebaseDataRefAndInstance;
import com.sjsu.boreas.ViewHolder.UsersViewHolder;
import com.sjsu.boreas.database.AppDatabase;
import com.sjsu.boreas.database.User;
import com.sjsu.boreas.messaging.ChatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddContactActivity extends AppCompatActivity {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------ADdContactActivity ";

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<User, UsersViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LandingPage mParent;
    private Handler handler;


//    private DatabaseReference database_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        initUI();
    }

    private void initUI() {
        Log.e(TAG, SUB_TAG+"initUI");
        recyclerView = findViewById(R.id.online_ppl_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        manageMessageFromWorkerThread();

        initializeFirebaseAdapter();

        recyclerView.setAdapter(mAdapter);
    }

    public void initializeFirebaseAdapter(){
        Log.e(TAG, SUB_TAG+"InitializeFirebase adapter");
        Query query = FirebaseDatabase.getInstance().getReference().child("users");

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Log.e(TAG, SUB_TAG+"Parse snapshot");
                        if(snapshot.child("uid").getValue().toString().equals(MainActivity.currentUser.getUid()))
                            Log.e(TAG, SUB_TAG+"This is my user: " + MainActivity.currentUser.getName());
                        return new User(snapshot.child("uid").getValue().toString(),
                                snapshot.child("name").getValue().toString(),
                                Double.parseDouble(snapshot.child("latitude").getValue().toString()),
                                Double.parseDouble(snapshot.child("longitude").getValue().toString()),
                                false);
                    }
                })
                .build();

        mAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                Log.e(TAG, SUB_TAG+"onCreateViewHolder");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder holder, int position, @NonNull final User model) {
//                Log.e(TAG, SUB_TAG+"onBindViewHolder");
                holder.bindToListItemView(model);

                if(model.getUid().equals(MainActivity.currentUser.getUid())) {
                    Log.e(TAG, SUB_TAG+"Hiding item for user: " + MainActivity.currentUser.getName());
                    holder.hideThisView();
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Log.e(TAG, SUB_TAG+"999999999999999999999999 this is the selected user: " + model.getUid());
                        addContact(model);
                    }
                });
            }
        };
    }

    private void addContact(final User user){
        Log.e(TAG, SUB_TAG+"addContact");

        Map<String, Object> new_user = user.toMap();
        Map<String, Object> firebase_child_update = new HashMap<>();

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/contacts/" + MainActivity.currentUser.getUid() + "/" + user.getUid(), new_user);

        Log.e(TAG, SUB_TAG+"My user ID is: " + MainActivity.currentUser.getUid() + ", and the contact id is: " + user.getUid());

        //Do the actual writing of the data onto firebase and locally
        FirebaseDataRefAndInstance.getDatabaseReference().updateChildren(firebase_child_update);
        AsyncTask.execute(new Runnable() {
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
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private void manageMessageFromWorkerThread(){
        Log.e(TAG, SUB_TAG+"***********************Manage messg from worker thread");
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Log.e(TAG, SUB_TAG+"&&&&&&&&&&&&&&&&&&&&&&: " + message.obj);
//                Toast.makeText(getApplicationContext(), (Integer) message.obj, Toast.LENGTH_LONG).show();
            }
        };
    }
}
