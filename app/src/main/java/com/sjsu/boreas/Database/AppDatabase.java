package com.sjsu.boreas.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.Messages.ChatMessageDao;
import com.sjsu.boreas.Database.Users.User;
import com.sjsu.boreas.Database.Users.UserDao;

@Database(entities= {User.class, ChatMessage.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ChatMessageDao chatMessageDao();
}