package com.sjsu.boreas.UserRecyclerViewStuff;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.R;
import com.sjsu.boreas.Database.Users.User;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------UsersViewHolder";

    public View rootView;
    public TextView name;
    public TextView txtDesc;
    private String uid;
    private View newMessageIndicator;

    public UsersViewHolder(@NonNull View itemView) {
        super(itemView);
        Log.e(TAG, SUB_TAG+"UsersViewHolder");
        this.rootView = itemView;
        this.name = itemView.findViewById(R.id.userName);
        this.newMessageIndicator = itemView.findViewById(R.id.newMessage);
    }

    public void bindToListItemView(User user) {
        Log.e(TAG, SUB_TAG+"Bind to Post");
        Log.e(TAG, SUB_TAG+"User passed: "+ user.getUid());
        this.name.setText(user.getName());
        this.uid = user.getUid();

        if(user.newMessage){
            newMessageIndicator.setVisibility(View.VISIBLE);
        }
        else{
            newMessageIndicator.setVisibility(View.INVISIBLE);
        }
    }

    // what is this for?
    public void hideThisView(){
        rootView.setVisibility(View.GONE);
        rootView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
    }

    public String getUID(){
        return uid;
    }
}
