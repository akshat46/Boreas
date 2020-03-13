package com.sjsu.boreas.messages;

import com.sjsu.boreas.database.User;

import java.io.Serializable;

/**
 * Represents a message that can be sent through Google Nearby.
 */
public class NearbyMessage implements Serializable {
    public User sender;
    public long timestamp;

    public NearbyMessage(User myuser){
        sender = myuser;
        timestamp = System.currentTimeMillis();
    }
}