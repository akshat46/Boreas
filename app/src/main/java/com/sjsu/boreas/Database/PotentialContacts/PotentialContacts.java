package com.sjsu.boreas.Database.PotentialContacts;

import androidx.room.Entity;

import com.sjsu.boreas.Database.Users.User;

import java.io.Serializable;

@Entity
public class PotentialContacts extends User implements Serializable {

    public PotentialContacts(String uid, String name, double latitude, double longitude, boolean isMe) {
        super(uid, name, latitude, longitude, isMe);
    }
}
