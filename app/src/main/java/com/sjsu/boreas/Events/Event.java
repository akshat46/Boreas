package com.sjsu.boreas.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Event {
    private static List<Event> events;

    private String event_id;
    private List<EventListener> listeners = new ArrayList<>();

    public static Event get(String e_id){
        e_id = e_id.toLowerCase();
        for(Event e : events){
            if(e.getID().equals(e_id)) return e;
        }
        return new Event(e_id);
    }

    private Event(String id){
        this.event_id = id;
        events.add(this);
    }

    public void addListener(EventListener l){
        if(!this.listeners.contains(l)) this.listeners.add((l));
    }

    public void deleteListener (EventListener l){
        this.listeners.remove(l);
    }

    public String getID(){
        return this.event_id;
    }

    public void trigger(HashMap<String, Object> packet){
        for(EventListener l : listeners){
            l.eventTriggered(packet);
        }
    }
}
