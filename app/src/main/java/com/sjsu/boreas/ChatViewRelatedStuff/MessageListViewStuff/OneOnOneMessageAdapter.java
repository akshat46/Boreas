package com.sjsu.boreas.ChatViewRelatedStuff.MessageListViewStuff;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.SubMenuBuilder;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.ChatViewRelatedStuff.ChatBubble;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.UserRecyclerViewStuff.UsersViewHolder;

import java.util.List;

//This class is modeled after the ChatMessageAdapter class
//TODO: maybe we can have some common class which both classes inherrit from
public class OneOnOneMessageAdapter extends RecyclerView.Adapter<OneOnOneMessageViewHolder> {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------OneOneOneMessageAdapter-- ";

    private List<ChatMessage> chatMessages;
    private ViewGroup parent;

    private static int NOT_MY_MSSG = 0;
    private static int MY_MSSG = 1;

    public OneOnOneMessageAdapter(List<ChatMessage> chatMessages) {
        Log.e(TAG, SUB_TAG+"constructor of oneOnOneMessg adapter");
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public OneOnOneMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(TAG, SUB_TAG+"onCreateViewHolder");
        this.parent = parent;
        View view;

        if (viewType == MY_MSSG) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_bubble_right, parent, false);
            return new OneOnOneMessageViewHolder(view);
        } else if (viewType == NOT_MY_MSSG) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_bubble_left, parent, false);
            return new OneOnOneMessageViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull OneOnOneMessageViewHolder holder, int position) {
        Log.e(TAG, SUB_TAG+"onBindViewHolder");
        ChatMessage chatMessage = chatMessages.get(position);
        holder.bindToListItemView(chatMessage);
    }

    @Override
    public int getItemViewType(int position) {
        // return a value between 0 and (getViewTypeCount - 1)
        Log.e(TAG, SUB_TAG+"Checking if this is my message");
        if(chatMessages.get(position).senderId.equals(MainActivity.currentUser.getUid())){
            Log.e(TAG, SUB_TAG+"This message is from me");
            return MY_MSSG;
        }

        return NOT_MY_MSSG;
    }

    //@Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        Log.e(TAG, SUB_TAG+"get view");
//        OneOnOneMessageViewHolder holder;
//        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//
//        int layoutResource = 0; // determined by view type
//
//    }

    private boolean isTheMssgFromMe(ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Checking if this is my message");
        if(mssg.senderId.equals(MainActivity.currentUser.getUid())){
            Log.e(TAG, SUB_TAG+"This message is from me");
            return true;
        }

        return false;
    }

    //@Override
    public int getViewTypeCount() {
        // return the total number of view types. this value should never change
        // at runtime. Value 2 is returned because of left and right views.
        return 2;
    }

    //@Override
    public int getCount() {
        return 0;
    }

    //@Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


}