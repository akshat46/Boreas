package com.sjsu.boreas.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.AddContactActivity;
import com.sjsu.boreas.Firebase.FirebaseDataRefAndInstance;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.R;
import com.sjsu.boreas.ViewHolder.UsersViewHolder;
import com.sjsu.boreas.database.User;

import java.util.ArrayList;
import java.util.List;

public class AdapterForFirebase extends RecyclerView.Adapter<UsersViewHolder>{

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "------AdapterForFirebase-----";
    private List<User>userList;

    public AdapterForFirebase(ArrayList<User> ul){
        this.userList = ul;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e(TAG, SUB_TAG+"onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        Log.e(TAG, SUB_TAG+"onBindViewHolder");
        final User model = userList.get(position);
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
                AddContactActivity.addContact(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}
