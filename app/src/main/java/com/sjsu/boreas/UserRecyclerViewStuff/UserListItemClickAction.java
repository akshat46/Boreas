package com.sjsu.boreas.UserRecyclerViewStuff;

import com.sjsu.boreas.Database.Contacts.User;

public interface UserListItemClickAction {
    public void onItemClicked(User model, int position);
}
