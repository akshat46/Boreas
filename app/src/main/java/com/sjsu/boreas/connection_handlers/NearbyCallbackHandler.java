package com.sjsu.boreas.connection_handlers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.database.User;
import com.sjsu.boreas.messages.AdjacencyListMessage;
import com.sjsu.boreas.messages.TextMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class NearbyCallbackHandler {

    private NearbyConnectionHandler connectionHandler;
    private HashMap<String, String> connectedUsers; //Map endpointId to userId


    public NearbyCallbackHandler(NearbyConnectionHandler act){
        connectionHandler = act;
        connectedUsers = new HashMap<>();
    }

    //Receiving payloads
    public PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            try {
                System.out.println(payload.getType());
                //ByteArrayInputStream bytes = new ByteArrayInputStream();
                ObjectInputStream stream = new ObjectInputStream(payload.asStream().asInputStream());
                Object result = stream.readObject();
                if(result instanceof AdjacencyListMessage){
                    AdjacencyListMessage message = (AdjacencyListMessage) result;
                    //Check if received adjacency list overlaps current adjacency list
                    boolean isMeshMember = false;
                    HashSet<String> adjacencySet = createIdSet(connectionHandler.meshMembers);
                    for(String s : message.adjacentIds){
                        if(adjacencySet.contains(s)){
                            isMeshMember = true;
                            break;
                        }
                    }
                    if(isMeshMember){
                        connectionHandler.getClient().disconnectFromEndpoint(endpointId);
                        return;
                    }

                }else if(result instanceof TextMessage){
                    TextMessage message = (TextMessage) result;
                    connectionHandler.receiveMessage(message);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
            //connectionHandler.receiveMessage(null, WifiConfiguration.Status.strings[update.getStatus()]);
        }
    };

    //Discovering other devices
    public EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            //Map endpointId to userId
            connectedUsers.put(endpointId, discoveredEndpointInfo.getEndpointName());
            MainActivity.makeLog("Network endpoint discovered, connecting to "+endpointId+"/"+discoveredEndpointInfo.getEndpointName());
            //connectionHandler.receiveMessage(null, "Network endpoint discovered, connecting to "+discoveredEndpointInfo.getEndpointName());
            connectionHandler.getClient().requestConnection(connectionHandler.getDeviceName(), endpointId, connectionLifecycleCallback);
            System.out.println("Found device!");
        }

        @Override
        public void onEndpointLost(@NonNull String endpointId) {
            MainActivity.makeLog("Lost endpoint: "+endpointId);
        }
    };

    public ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            //Map endpointId to userId
            connectedUsers.put(endpointId, connectionInfo.getEndpointName());
            MainActivity.makeLog("Accepting connection with "+endpointId+"/"+connectionInfo.getEndpointName());
            connectionHandler.getClient().acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if(result.getStatus().isSuccess()){
                MainActivity.makeLog("Successful connection to "+endpointId);
                //connectionHandler.setConnectionEndpointName(endpointId);

                //Add new neighbor to list/map of neighbors
                connectionHandler.neighbors.put(connectedUsers.get(endpointId), endpointId);
                //Construct adjacency list message with current user object and set of mesh member userIds
                AdjacencyListMessage message = new AdjacencyListMessage(MainActivity.currentUser,
                        createIdList(connectionHandler.meshMembers));
                connectionHandler.getClient().sendPayload(endpointId, Payload.fromStream(constructStreamFromSerializable(message)));
            }else{
                MainActivity.makeLog("Connection failed with "+endpointId);
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            MainActivity.makeLog("Disconnected from "+endpointId);
            connectionHandler.neighbors.remove(connectedUsers.get(endpointId));
            connectionHandler.meshMembers.remove(connectedUsers.get(endpointId));
            connectedUsers.remove(endpointId);
        }


    };

    class ConnectedUser {
        public String endpointId;
        public User user;

        public ConnectedUser(String endpointId, User user){
            this.endpointId = endpointId;
            this.user = user;
        }
    }


    public HashSet<String> createIdSet(HashMap<String, HashSet<String>> adjacencies){
        HashSet<String> allUsers = new HashSet<>();
        for(String neighbor : adjacencies.keySet()){
            for(String uid : adjacencies.get(neighbor)){
                allUsers.add(uid);
            }
        }
        return allUsers;
    }

    public String[] createIdList(HashMap<String, HashSet<String>> adjacencies){
        LinkedList<String> userList = new LinkedList<>();
        HashSet<String> allUsers = createIdSet(adjacencies);
        Iterator<String> iter = allUsers.iterator();
        while(iter.hasNext()){
            userList.add(iter.next());
        }
        return userList.toArray(new String[0]);
    }

    public InputStream constructStreamFromSerializable(Serializable obj){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(baos);
            stream.writeObject(obj);
            stream.flush();
            stream.close();

            return new ByteArrayInputStream(baos.toByteArray());
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
}
