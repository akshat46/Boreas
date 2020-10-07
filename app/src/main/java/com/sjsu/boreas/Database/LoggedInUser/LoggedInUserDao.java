package com.sjsu.boreas.Database.LoggedInUser;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface LoggedInUserDao {

    @Query("SELECT * FROM loggedinuser WHERE isLoggedIn is 1")
    LoggedInUser getLoggedInUser();

    @Query("SELECT * FROM loggedinuser WHERE uid is :uid AND password is :password")
    LoggedInUser getUserWithGivenCredentials(String uid, String password);

    @Query("SELECT * FROM loggedinuser")
    LoggedInUser getRegisteredUser();

    @Query("SELECT * FROM loggedinuser WHERE uid is :uid")
    LoggedInUser checkIfUserIsAlreadyRegistered(String uid);

    @Query("DELETE FROM loggedinuser")
    void clearLoggedInUserTable();

    @Update
    void logUserIn(LoggedInUser user);

    @Update
    void logUserOut(LoggedInUser user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNewUser(LoggedInUser user);

    @Delete
    void deleteUser(LoggedInUser user);

}
