package com.sjsu.boreas.Database.PotentialContacts;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sjsu.boreas.Database.Users.User;

import java.util.List;

@Dao
public interface PotentialContactsDao {

    @Query("SELECT * FROM potentialcontacts WHERE me is 0")
    List<User> getUsers();

    @Query("SELECT * FROM potentialcontacts WHERE uid is :userID")
    List<User> getSpecificUser(String userID);

    @Query("SELECT * FROM potentialcontacts WHERE me is 1 ORDER BY abs(SQUARE(latitude) + SQUARE(longitude) - SQUARE(:lat) - SQUARE(:lon)) ASC")
    List<User> getClosestUsers(double lat, double lon);

    @Insert
    void insertAll(PotentialContacts... potentialContacts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNewUser(PotentialContacts potentialContacts);

    @Delete
    void delete(PotentialContacts potentialContacts);
}
