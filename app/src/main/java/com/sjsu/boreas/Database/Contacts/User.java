package com.sjsu.boreas.Database.Contacts;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.sjsu.boreas.Database.Messages.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
public class User implements Serializable {

    private static final String TAG = "BOREAS";
    private static final String SUB_TAG = "-------User cleass----- ";

    public User(String uid, String name, double latitude, double longitude){
        this.uid = uid;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @NonNull
    @PrimaryKey
    public String uid;

    @ColumnInfo(name = "newMessage")
    public boolean newMessage = false;

    @ColumnInfo(name = "time")
    public long lastMessageTime = -1;

    @ColumnInfo(name = "lastMessage")
    public String lastMessage;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "publicKey")
    public String publicKey;

    public String toString(){
        return name+": "+uid+"\n"+latitude + " , " + longitude+"\n";
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("publicKey", publicKey);
        return result;
    }

    public String getUid(){
        return uid;
    }
    public String getName(){return name;}
    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}

    public static User convertHashMapToUser(HashMap<String, Object> user_map){
        Log.e(TAG, SUB_TAG+"converting a hash map to user object.");

        User contact = null;

        String contact_name, contact_id;
        double contact_lat, contact_lon;

        contact_lat = (double) user_map.get("latitude");
        contact_lon = (double) user_map.get("longitude");
        contact_name = (String) user_map.get("name");
        contact_id = (String) user_map.get("uid");

        contact = new User(contact_id, contact_name, contact_lat, contact_lon);

        return contact;

    }
}
