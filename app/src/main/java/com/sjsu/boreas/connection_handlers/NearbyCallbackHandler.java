package com.sjsu.boreas.connection_handlers;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.sjsu.boreas.connection_handlers.NearbyConnectionHandler;

import java.util.HashMap;

public class NearbyCallbackHandler {

    private NearbyConnectionHandler activity;
    private HashMap<String, String> connectedUsers; //Map endpointId to username

    public NearbyCallbackHandler(NearbyConnectionHandler act){
        activity = act;
        connectedUsers = new HashMap<>();
    }

    //Receiving payloads
    public PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            String sender = connectedUsers.get(endpointId);
            if(sender == null)
                sender = endpointId;
            activity.receiveMessage(sender, new String(payload.asBytes()));
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
            //activity.receiveMessage(null, WifiConfiguration.Status.strings[update.getStatus()]);
        }
    };

    //Discovering other devices
    public EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            connectedUsers.put(endpointId, discoveredEndpointInfo.getEndpointName());
            activity.receiveMessage(null, "Network endpoint discovered, connecting to "+discoveredEndpointInfo.getEndpointName());
            activity.getClient().requestConnection(activity.getDeviceName(), endpointId, connectionLifecycleCallback);
            System.out.println("Found device!");
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            activity.receiveMessage(null, "Lost endpoint: "+s);
        }
    };

    public ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            connectedUsers.put(endpointId, connectionInfo.getEndpointName());
            activity.receiveMessage(null, "Accepting connection with "+connectionInfo.getEndpointName());
            activity.getClient().acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if(result.getStatus().isSuccess()){
                activity.receiveMessage(null, "Successful Connection");
                //activity.getClient().stopAdvertising();
                //activity.getClient().stopDiscovery();

                activity.setConnectionEndpointName(endpointId);

            }else{
                activity.receiveMessage(null, "Connection Failed");
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            activity.receiveMessage(null, "Disconnected from "+endpointId);
        }
    };

    class ConnectedUser {
        public String endpointId;
        public String userId;

        public ConnectedUser(String endpointId, String userId){
            this.endpointId = endpointId;
            this.userId = userId;
        }
    }
}
