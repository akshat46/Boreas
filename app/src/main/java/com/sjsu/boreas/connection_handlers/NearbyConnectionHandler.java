package com.sjsu.boreas.connection_handlers;

import android.app.Activity;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;
import com.sjsu.boreas.messaging.ChatActivity;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class NearbyConnectionHandler {

    private static final int ADVERTISE_PERIOD = 10000; //10 seconds
    private static final int DISCOVER_PERIOD = 120000; //2 minutes

    private Activity context;
    public ChatActivity chatActivity;
    String deviceName;
    private NearbyCallbackHandler handlerNearby;
    private ConnectionsClient client;
    private String endpointName;

    private Timer timer;
    private TimerTask taskAdvertise;
    private int connectionState; //Records state of the handler.
    // 0 = just started, initial discovery, 1 = only advertising mode, 2 = advertising and discovering
    private boolean isAdvertising, isDiscovering;

    private HashSet<String> meshMembers; //Members of current connection mesh

    public NearbyConnectionHandler(Activity context){
        this.context = context;

        deviceName = "device: "+((int)(Math.random()*100));
        handlerNearby = new NearbyCallbackHandler(this);
        client= Nearby.getConnectionsClient(context);

        timer = new Timer();
        meshMembers = new HashSet<>();

        //startAdvertising();
        //startDiscovering();
        advanceConnectionState();
    }

    public void advanceConnectionState(){
        switch(connectionState){
            case 0:
                startAdvertising();
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
                client.stopAdvertising();
                isAdvertising = false;
                startDiscovering();
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

    public void sendMessage(String text) {
        receiveMessage(null, text);
        client.sendPayload(endpointName, Payload.fromBytes(text.getBytes()));
    }

    public void receiveMessage(String sender, String text) {
        chatActivity.addMessage(sender == null, sender, text);
    }
}
