package com.sjsu.boreas.connection_handlers;

import android.app.Activity;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.sjsu.boreas.LandingPage;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.ViewFragments.OfflineGroupFragment;
import com.sjsu.boreas.ViewFragments.OneOnOneFragment;
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

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---NearbyConnectionHandler ";

    private Activity context;
    public Fragment activeFrag;
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
        Log.e(TAG, SUB_TAG+"NearbyConnectionHandler");
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
        Log.e(TAG, SUB_TAG+"advanceConnectionState");
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
        Log.e(TAG, SUB_TAG+"onStart");
        advanceConnectionState();
    }

    public void onStop(){
        Log.e(TAG, SUB_TAG+"onStop");
        client.stopAllEndpoints();
        isAdvertising = false;
        isDiscovering = false;
        connectionState = 0;
    }

    public void startAdvertising(){
        Log.e(TAG, SUB_TAG+"startAdvertising");
        if(isAdvertising)
            return;
        isAdvertising = true;
        client.startAdvertising(deviceName, context.getPackageName(), handlerNearby.connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
        System.out.println("Advertising");
    }

    public void startDiscovering(){
        Log.e(TAG, SUB_TAG+"startDiscovering");
        if(isDiscovering)
            return;
        isDiscovering = true;
        client.startDiscovery(context.getPackageName(), handlerNearby.endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
        System.out.println("Discovering");
    }

    public void startBroadcast(){
        Log.e(TAG, SUB_TAG+"startBroadcast");
        String payloadStr = "Hello from "+deviceName+"!";
        client.sendPayload(endpointName, Payload.fromBytes(payloadStr.getBytes()));
        System.out.println("Sent payload!");
    }

    //Helper Functions
    public String getDeviceName(){
        Log.e(TAG, SUB_TAG+"getDeviceName");
        return deviceName;
    }

    public void setConnectionEndpointName(String name){
        Log.e(TAG, SUB_TAG+"setConnectionEndpointName");
        endpointName = name;
    }

    public ConnectionsClient getClient(){
        Log.e(TAG, SUB_TAG+"ConnectionsClient");
        return client;
    }

    public void setActiveActivity(Activity act){
        Log.e(TAG, SUB_TAG+"setActiveActivity");
        activeActivity = act;
    }

    public void removeActiveActivity(){
        Log.e(TAG, SUB_TAG+"removeActiveActivity");
        activeActivity = null;
    }

    public void setActiveFragment(Fragment act){
        Log.e(TAG, SUB_TAG+"Set active frag");
        activeFrag = act;
    }

    public void removeActiveFragment(){
        Log.e(TAG, SUB_TAG+"removeACtiveFrag");
        activeFrag = null;
    }

    public List<TextMessage> dequeueGroupChats(){
        Log.e(TAG, SUB_TAG+"dequeueGroupChats");
        ArrayList<TextMessage> messages = new ArrayList<>(groupchatQueue.size());
        while(!groupchatQueue.isEmpty())
            messages.add(groupchatQueue.remove());
        return messages;
    }

    public void sendGroupMessage(String text){
        Log.e(TAG, SUB_TAG+"sendGroupMessage");
        TextMessage message = new TextMessage(MainActivity.currentUser, true, text);
        if(activeActivity instanceof LandingPage && activeFrag instanceof OfflineGroupFragment){
            Log.e(TAG, SUB_TAG+"Active activity is Landing Page");
            ((OfflineGroupFragment) activeFrag).addMessage(true, null, text);
        }

        for(String neighbor : neighbors.keySet()){
            client.sendPayload(neighbors.get(neighbor), Payload.fromStream(handlerNearby.constructStreamFromSerializable(message)));
        }

    }

    public void receiveMessage(TextMessage message){
        Log.e(TAG, SUB_TAG+"receiveMessage");
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
        Log.e(TAG, SUB_TAG+"sendMessage");
        receiveMessage(null, text);
        client.sendPayload(endpointName, Payload.fromBytes(text.getBytes()));
    }

    public void receiveMessage(String sender, String text) {
        Log.e(TAG, SUB_TAG+"receiveMessage");
        chatActivity.addMessage(sender == null, sender, text);
    }
}
