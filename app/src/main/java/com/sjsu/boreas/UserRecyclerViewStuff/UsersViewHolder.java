package com.sjsu.boreas.UserRecyclerViewStuff;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.HelperStuff.ContextHelper;
import com.sjsu.boreas.R;
import com.sjsu.boreas.Database.Contacts.User;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------UsersViewHolder";

    public View rootView;
    public TextView name;
    public TextView lastMessage;
    private String uid;
    private View newMessageIndicator;
    public Context context = ContextHelper.get().getApplicationContext();

    public UsersViewHolder(@NonNull View itemView) {
        super(itemView);
        Log.e(TAG, SUB_TAG+"UsersViewHolder");
        this.rootView = itemView;
        this.name = itemView.findViewById(R.id.userName);
        this.newMessageIndicator = itemView.findViewById(R.id.newMessage);
        this.lastMessage = itemView.findViewById(R.id.lastMessage);
    }

    public void bindToListItemView(User user) {
        Log.e(TAG, SUB_TAG+"Bind to Post");
        Log.e(TAG, SUB_TAG+"User passed: "+ user.getUid());
        this.name.setText(user.getName());
        this.uid = user.getUid();

        if(user.lastMessage != null && !user.lastMessage.trim().isEmpty()){
            this.lastMessage.setText(user.lastMessage);
        }
        else this.lastMessage.setVisibility(View.INVISIBLE);

        if(user.newMessage){
            newMessageIndicator.setVisibility(View.VISIBLE);
            this.lastMessage.setTypeface(this.lastMessage.getTypeface(), Typeface.BOLD);
            this.lastMessage.setTextColor(context.getResources().getColor(R.color.colorTitle));
        }
        else{
            newMessageIndicator.setVisibility(View.INVISIBLE);
            this.lastMessage.setTypeface(this.lastMessage.getTypeface(), Typeface.NORMAL);
            this.lastMessage.setTextColor(context.getResources().getColor(R.color.colorText));
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
