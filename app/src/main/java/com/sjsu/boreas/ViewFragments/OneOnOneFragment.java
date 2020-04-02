package com.sjsu.boreas.ViewFragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.Query;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.boreas.ChatViewRelatedStuff.OneOnOneChatViewAdapter;
import com.sjsu.boreas.Firebase.GetFirebaseContacts;
import com.sjsu.boreas.LandingPage;
import com.sjsu.boreas.R;
import com.sjsu.boreas.ViewHolder.UsersViewHolder;
import com.sjsu.boreas.database.User;
import com.sjsu.boreas.messaging.ChatActivity;
import com.sjsu.boreas.messaging.ChatMessageAdapter;

public class OneOnOneFragment extends Fragment {
    public static final String EXTRA_TAB_NAME = "tab_name";
    private String mTabName;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<User, UsersViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LandingPage mParent;
    private View rootView;
    private Context mContext;

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---OneOnOne___Frag ";

    public OneOnOneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
                Log.e(TAG, SUB_TAG+"onCreateViewHolder");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_chat, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User model) {
                Log.e(TAG, SUB_TAG+"onBindViewHolder");
                holder.bindToListItemView(model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        startActivity(intent);
                    }
                });
            }
        };
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

}
