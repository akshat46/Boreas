package com.sjsu.boreas.UserRecyclerViewStuff;

import com.sjsu.boreas.Database.Users.User;

public interface UserListItemClickAction {
    public void onItemClicked(User model);
}
