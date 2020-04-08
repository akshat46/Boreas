package com.sjsu.boreas.ChatViewRelatedStuff;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.Firebase.GetFirebaseContacts;
import com.sjsu.boreas.R;
import com.sjsu.boreas.database.User;

public class OneOnOneChatViewAdapter extends RecyclerView.Adapter<OneOnOneChatViewAdapter.MyViewHolder> {


    private static String TAG = "BOREAS";
    private static String SUB_TAG = "--------OneonOneChatViewADapter ";
    private User[] userContactList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout textView;
        public MyViewHolder(RelativeLayout v) {
            super(v);
            Log.e(TAG, SUB_TAG+"MyViewHolder inner static class");
            textView = v;
        }
    }

    public OneOnOneChatViewAdapter(){
        Log.e(TAG, SUB_TAG+"OneOnOneChatView adapter");
        GetFirebaseContacts getFirebaseContacts = new GetFirebaseContacts();
        this.userContactList  = getFirebaseContacts.getMyFirebaseContacts();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public OneOnOneChatViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        Log.e(TAG, SUB_TAG+"OneOnOneChatViewAdapter my viewholder");
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.e(TAG, SUB_TAG+"onBindViewHolder");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.e(TAG, SUB_TAG+"getItem Count");
        return 20;
    }
}