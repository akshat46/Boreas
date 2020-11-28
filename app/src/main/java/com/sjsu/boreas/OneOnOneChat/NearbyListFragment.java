package com.sjsu.boreas.OneOnOneChat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sjsu.boreas.ChatView.ChatActivity;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.LandingPage;

public class NearbyListFragment extends OneOnOneFragment {
    private LandingPage mParent;
    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---Nearby___Frag ";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO: add event for temp chatmessage,users added/removed
        super.onCreate(savedInstanceState);
        mParent = (LandingPage) getActivity();
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
// TODO: Initialize adapter
//        populate adaptercontentList here. Async?
//        mAdapter=new UserListAdapter(adapterContentList, mContext, userListItemClickAction);
//        mParent.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                recyclerView.setAdapter(mAdapter);
//            }
//        });
    }

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
