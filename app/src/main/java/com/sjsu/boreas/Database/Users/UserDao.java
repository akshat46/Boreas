package com.sjsu.boreas.Database.Users;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM user WHERE me is 1")
    List<User> getMe();

    @Query("SELECT * FROM user WHERE me is 0")
    List<User> getUsers();

    @Query("SELECT * FROM user WHERE uid is :userID")
    List<User> getSpecificUser(String userID);

    @Query("SELECT * FROM user WHERE me is 1 ORDER BY abs(SQUARE(latitude) + SQUARE(longitude) - SQUARE(:lat) - SQUARE(:lon)) ASC")
    List<User> getClosestUsers(double lat, double lon);

    @Insert
    void insertAll(User... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNewUser(User user);

    @Delete
    void delete(User user);
}
