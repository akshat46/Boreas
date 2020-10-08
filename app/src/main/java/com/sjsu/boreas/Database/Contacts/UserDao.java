package com.sjsu.boreas.Database.Contacts;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user")
    List<User> getUsers();

    @Query("SELECT * FROM user WHERE uid is :userID")
    List<User> getSpecificUser(String userID);

    @Query("SELECT * FROM user ORDER BY abs(SQUARE(latitude) + SQUARE(longitude) - SQUARE(:lat) - SQUARE(:lon)) ASC")
    List<User> getClosestUsers(double lat, double lon);

    @Insert
    void insertAll(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNewUser(User user);

    @Delete
    void delete(User user);
}