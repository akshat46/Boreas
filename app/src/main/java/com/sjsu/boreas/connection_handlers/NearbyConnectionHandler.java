package com.sjsu.boreas.connection_handlers;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.database.User;
import com.sjsu.boreas.messages.TextMessage;
import com.sjsu.boreas.messaging.ChatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NearbyConnectionHandler {

    private static final int ADVERTISE_PERIOD = 10000; //10 seconds
    private static final int DISCOVER_PERIOD = 120000; //2 minutes
    private static final int MAX_GROUPCHAT_FORWARDS = 3; //max hops a group chat message will be propagated

    private Activity context;
    public ChatActivity chatActivity;
    String deviceName;
    private NearbyCallbackHandler handlerNearby;
    private ConnectionsClient client;
    private String endpointName;

    private Activity activeActivity;
    private LinkedList<TextMessage> groupchatQueue;
    private LinkedList<TextMessage> peermessageQueue;

    private Timer timer;
    private TimerTask taskAdvertise;
    private int connectionState; //Records state of the handler.
    // 0 = just started, initial discovery, 1 = only advertising mode, 2 = advertising and discovering
    private boolean isAdvertising, isDiscovering;

    protected HashMap<String, HashSet<String>> meshMembers; //Members of current connection mesh (userId neighbor -> mesh member userIds)
    protected HashMap<String, String> neighbors; //neighbor userId to endpointId

    public NearbyConnectionHandler(Activity context){
        this.context = context;

        groupchatQueue = new LinkedList<>();
        peermessageQueue = new LinkedList<>();

        deviceName = MainActivity.currentUser.uid;
        handlerNearby = new NearbyCallbackHandler(this);
        client= Nearby.getConnectionsClient(context);

        timer = new Timer();
        meshMembers = new HashMap<>();
        neighbors = new HashMap();

        //startAdvertising();
        //startDiscovering();
        advanceConnectionState();
    }

    public void advanceConnectionState(){
        switch(connectionState){
            case 0:
                startDiscovering();
                taskAdvertise = new TimerTask() {
                    @Override
                    public void run() {
                        connectionState = 1;
                        advanceConnectionState();
                    }
                };

                timer.schedule(taskAdvertise, ADVERTISE_PERIOD);
                break;
            case 1:
                client.stopDiscovery();
                isDiscovering = false;
                startAdvertising();
                taskAdvertise = new TimerTask() {
                    @Override
                    public void run() {
                        connectionState = 0;
                        advanceConnectionState();
                    }
                };
                timer.schedule(taskAdvertise, DISCOVER_PERIOD);
                break;
        }

    }

    public void onStart(){
        advanceConnectionState();
    }

    public void onStop(){
        client.stopAllEndpoints();
        isAdvertising = false;
        isDiscovering = false;
        connectionState = 0;
    }

    public void startAdvertising(){
        if(isAdvertising)
            return;
        isAdvertising = true;
        client.startAdvertising(deviceName, context.getPackageName(), handlerNearby.connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
        System.out.println("Advertising");
    }

    public void startDiscovering(){
        if(isDiscovering)
            return;
        isDiscovering = true;
        client.startDiscovery(context.getPackageName(), handlerNearby.endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
        System.out.println("Discovering");
    }

    public void startBroadcast(){
        String payloadStr = "Hello from "+deviceName+"!";
        client.sendPayload(endpointName, Payload.fromBytes(payloadStr.getBytes()));
        System.out.println("Sent payload!");
    }

    //Helper Functions
    public String getDeviceName(){
        return deviceName;
    }

    public void setConnectionEndpointName(String name){
        endpointName = name;
    }

    public ConnectionsClient getClient(){
        return client;
    }

    public void setActiveActivity(Activity act){
        activeActivity = act;
    }

    public void removeActiveActivity(){
        activeActivity = null;
    }

    public List<TextMessage> dequeueGroupChats(){
        ArrayList<TextMessage> messages = new ArrayList<>(groupchatQueue.size());
        while(!groupchatQueue.isEmpty())
            messages.add(groupchatQueue.remove());
        return messages;
    }

    public void sendGroupMessage(String text){
        TextMessage message = new TextMessage(MainActivity.currentUser, true, text);
        if(activeActivity instanceof ChatActivity){
            ((ChatActivity) activeActivity).addMessage(true, null, text);
        }

        for(String neighbor : neighbors.keySet()){
            client.sendPayload(neighbors.get(neighbor), Payload.fromStream(handlerNearby.constructStreamFromSerializable(message)));
        }

    }

    public void receiveMessage(TextMessage message){
        //Add to proper queue if need be
        if(message.isGroupchat && activeActivity instanceof ChatActivity){
            ((ChatActivity) activeActivity).addMessage(false, message.sender.name, message.message);
        }else{
            groupchatQueue.add(message);
        }

        User forwarder = message.forwarder;
        message.forwarder = MainActivity.currentUser;
        message.hops++;
        //If group chat, propagate it more
        if(message.isGroupchat && message.hops < MAX_GROUPCHAT_FORWARDS){
            System.out.println("Forwarding message from "+forwarder.name+": "+forwarder.uid);
            for(String neighbor : neighbors.keySet()){
                System.out.println("\t"+neighbor+" | "+neighbors.get(neighbor));
                if(neighbor.equals(forwarder.uid) || neighbors.get(neighbor).equals(forwarder.uid))
                    continue;
                MainActivity.makeLog("Sending payload to "+neighbor);
                client.sendPayload(neighbors.get(neighbor), Payload.fromStream(handlerNearby.constructStreamFromSerializable(message)));
            }
        }
    }

    public void sendMessage(String text) {
        receiveMessage(null, text);
        client.sendPayload(endpointName, Payload.fromBytes(text.getBytes()));
    }

    public void receiveMessage(String sender, String text) {
        chatActivity.addMessage(sender == null, sender, text);
    }
}
