package com.sjsu.boreas.Database.Users;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
public class User implements Serializable {

    public User(String uid, String name, double latitude, double longitude, boolean isMe, String hashedPassword){
        this.uid = uid;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isMe = isMe;
        this.password = hashedPassword;
    }

    public User(String uid, String name, double latitude, double longitude, boolean isMe){
        this.uid = uid;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isMe = isMe;
    }

    public String getName(){    return name;}

    @NonNull
    @PrimaryKey
    public String uid;

    @ColumnInfo(name = "me")
    public boolean isMe;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    //This is the hashed password
    @ColumnInfo(name = "password")
    public String password;

    public String toString(){
        return name+": "+uid+"\n"+latitude + " , " + longitude+"\n" + (isMe ? "IS" : "NOT") + " me";
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        if(password !=null)
            result.put("password", password);
        return result;
    }

    public String getUid(){
        return uid;
    }
}
