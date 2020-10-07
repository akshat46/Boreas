package com.sjsu.boreas.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.sjsu.boreas.Database.LoggedInUser.LoggedInUser;
import com.sjsu.boreas.Database.LoggedInUser.LoggedInUserDao;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.Messages.ChatMessageDao;
import com.sjsu.boreas.Database.PotentialContacts.PotentialContacts;
import com.sjsu.boreas.Database.PotentialContacts.PotentialContactsDao;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.Contacts.UserDao;

@Database(entities= {User.class, ChatMessage.class, PotentialContacts.class, LoggedInUser.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ChatMessageDao chatMessageDao();
    public abstract PotentialContactsDao potentialContactsDao();
    public abstract LoggedInUserDao loggedInUserDao();
}