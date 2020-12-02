package com.sjsu.boreas.OneOnOneChat;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.sjsu.boreas.ChatView.ChatActivity;
import com.sjsu.boreas.ContactRecyclerItems.UserListAdapter;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.Messages.MessageUtility;
import com.sjsu.boreas.Events.Event;
import com.sjsu.boreas.LandingPage;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.OfflineConnectionHandlers.NearbyConnectionHandler;
import com.sjsu.boreas.SettingsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class NearbyListFragment extends OneOnOneFragment {
    private LandingPage mParent;
    Event nbr_event;
    private boolean loading = false;
    private static String TAG = "BOREAS";
    private ImageButton im;
    private static String SUB_TAG = "---Nearby___Frag ";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Event.get(Event.CHAT_MSSG).addListener(this);
        nbr_event = Event.get(Event.NBR_UPDATED);
        nbr_event.addListener(this);
        mParent = (LandingPage) getActivity();

        // TODO: timer disabled for testing
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                refreshNeighbors();
//            }
//        }, 0, 30000);//refresh neighbors every 30 seconds

        super.onCreate(savedInstanceState);
    }

    public static NearbyListFragment newInstance(String tabName){
        Log.e(TAG, SUB_TAG+"OneOnOneFragment");
        Bundle args = new Bundle();
        args.putString(EXTRA_TAB_NAME, tabName);
        NearbyListFragment fragment = new NearbyListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeAdapter(){
        super.onStart();
        Log.e(TAG, SUB_TAG+"---- custom local list adapter");
        adapterContentList = new ArrayList<>();
        mAdapter=new UserListAdapter(adapterContentList, mContext, userListItemClickAction);
        mParent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(mAdapter);
            }
        });
        mParent.refreshList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshNeighbors();
            }
        });
    }

    // nearby people has 3 types:
    // 1. people who are also in your contacts -- show up as a normal user
    // 2. people who are in your potentialcontacts -- highlight in magenta
    // 3. people who have not had any contact with you -- new highlight

    @Override
    protected void manageMessage(ChatMessage mssg) {
        User user = mssg.sender;
        final int i = indexInContact(user);

        adapterContentList.get(i).newMessage = true;
        adapterContentList.get(i).lastMessage = mssg.mssgText;
        mParent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemChanged(i);
            }
        });
    }

    public void refreshNeighbors(){
        Log.e(TAG, SUB_TAG+"Refreshing neighbors list.. " + loading);
        if(!loading) {
            MainActivity.nearbyConnectionHandler.triggerNeighborRequest();
            mParent.refreshList.setVisibility(View.GONE);
            mParent.loading.setVisibility(View.VISIBLE);
            loading = true;
        }
        // only refresh if not loading already
    }

    @Override
    public void eventTriggered(HashMap<String, Object> packet, String type) {
        Log.e(TAG, SUB_TAG+"I have received a message, my lord. It goes something like: "+ packet.toString());
        if(type.equals(Event.CHAT_MSSG)) {
            Log.e(TAG, SUB_TAG+"this is a chat message event");
            ChatMessage mssg = MessageUtility.convertHashMapToChatMessage(packet);
            manageMessage(mssg);
        } else if(type.equals(Event.NBR_UPDATED)){
            Log.e(TAG, SUB_TAG+" neighbors list updating...");
            try {
                neighbors_updated((ArrayList<User>) packet.get("neighbors"), false);
            }
            catch (ClassCastException e){
                Log.e(TAG, SUB_TAG+"!! ERROR !! Neighbors updated event giving some weird ass packet.");
            }
        } else if(type.equals((nbr_event.getStarted()))){
            Log.e(TAG, SUB_TAG+" neighbors list updating started");
            loading = true;
        }
        else if(type.equals((nbr_event.getEnded()))){
            Log.e(TAG, SUB_TAG+" neighbors list updating ended");
            loading = false;
            mParent.refreshList.setVisibility(View.VISIBLE);
            mParent.loading.setVisibility(View.GONE);
            neighbors_updated((ArrayList<User>) packet.get("neighbors"), true);
        }
    }

    private void neighbors_updated(ArrayList<User> neighbors, boolean last){
        if(adapterContentList.isEmpty()) {
            Log.e(TAG, SUB_TAG+" filling adapterContentList with neighbors first time");
            adapterContentList.addAll(neighbors);
        }
        else{
            if(last){
                // remove the ones that are no longer in neighbors list
                for(User u : adapterContentList){
                    if (!neighbors.contains(u)) adapterContentList.remove(u);
                }
            }
            adapterContentList.addAll(neighbors);
            Log.e(TAG, SUB_TAG+" updating adapterContentList with neighbors");
            mParent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onItemClicked(User model,final int position) {
        if(model.newMessage) {
            adapterContentList.get(position).newMessage = false;
            mParent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyItemChanged(position);
                }
            });
        }
        // TODO: modified chatactivity? one that doesn't save chat messages in database/saves them separately
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        //Passing the user object using intent
        intent.putExtra("ReceiverObj", model);
        startActivity(intent);
    }
}
