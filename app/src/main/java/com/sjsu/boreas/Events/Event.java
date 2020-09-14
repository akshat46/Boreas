package com.sjsu.boreas.Events;

import android.util.Log;

import com.sjsu.boreas.ChatViewRelatedStuff.OneOnOneChatViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Event {
    private static List<Event> events = new ArrayList<>();
    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----Event class-- ";

    private String event_id;
    private List<EventListener> listeners = new ArrayList<>();

    public static Event get(String e_id){
        Log.e(TAG, SUB_TAG+"getting the instance");
        e_id = e_id.toLowerCase();
        for(Event e : events){
            if(e.getID().equals(e_id)) return e;
        }
        return new Event(e_id);
    }

    private Event(String id){
        Log.e(TAG, SUB_TAG+"Constructor");
        this.event_id = id;
        events.add(this);
    }

    public void addListener(EventListener l){
        Log.e(TAG, SUB_TAG+"adding listener: "+l.toString() + "\n\t\t\t" + l.getClass() + ", ");
        if(!this.listeners.contains(l.getClass())) this.listeners.add((l));
    }

    public void deleteListener (EventListener l){
        Log.e(TAG, SUB_TAG+"deleting listener: " + l);
        this.listeners.remove(l);
    }

    public String getID(){
        Log.e(TAG, SUB_TAG+"getting ID");
        return this.event_id;
    }

    public void trigger(HashMap<String, Object> packet){
        Log.e(TAG, SUB_TAG+"Trigger");
        int i = 0;
        for(EventListener l : listeners){
            Log.e(TAG, SUB_TAG+"In the loop, listener: " + l  + ", " + i++);
            l.eventTriggered(packet);
        }
    }
}
