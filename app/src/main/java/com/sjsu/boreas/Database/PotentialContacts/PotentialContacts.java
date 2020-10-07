package com.sjsu.boreas.Database.PotentialContacts;

import androidx.room.Entity;

import com.sjsu.boreas.Database.Contacts.User;

import java.io.Serializable;

@Entity
public class PotentialContacts extends User implements Serializable {

    public PotentialContacts(String uid, String name, double latitude, double longitude) {
        super(uid, name, latitude, longitude);
    }
}
