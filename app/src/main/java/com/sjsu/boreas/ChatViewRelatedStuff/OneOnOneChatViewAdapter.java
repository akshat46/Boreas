package com.sjsu.boreas.ChatViewRelatedStuff;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.ChatActivity2;
import com.sjsu.boreas.ChatNewActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.ViewFragments.OneOnOneFragment;

public class OneOnOneChatViewAdapter extends RecyclerView.Adapter<OneOnOneChatViewAdapter.MyViewHolder> {

private Context activity;
    public OneOnOneChatViewAdapter(Context activity) {
        this.activity = activity;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout textView;
        public MyViewHolder(RelativeLayout v) {
            super(v);
            textView = v;
        }
    }


    // Create new views (invoked by the layout manager)
    @Override
    public OneOnOneChatViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ChatNewActivity.class);
                activity.startActivity(intent);
            }
        });
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return 20;
    }
}