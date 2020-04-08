package com.sjsu.boreas.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sjsu.boreas.database.Messages.ChatMessage;
import com.sjsu.boreas.database.Messages.ChatMessageDao;

@Database(entities= {User.class, ChatMessage.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ChatMessageDao chatMessageDao();
}