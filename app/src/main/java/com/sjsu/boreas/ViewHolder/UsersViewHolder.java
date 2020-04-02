package com.sjsu.boreas.ViewHolder;

import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.R;
import com.sjsu.boreas.database.User;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------UsersViewHolder";

    public LinearLayout root;
    public TextView name;
    public TextView txtDesc;

    public UsersViewHolder(@NonNull View itemView) {
        super(itemView);
        Log.e(TAG, SUB_TAG+"UsersViewHolder");
        this.name = itemView.findViewById(R.id.nameTV);
    }

    public void bindToListItemView(User user) {
        Log.e(TAG, SUB_TAG+"Bind to Post");
        Log.e(TAG, SUB_TAG+"User passed: "+ user.getUid());
        this.name.setText(user.getName());
    }
}
