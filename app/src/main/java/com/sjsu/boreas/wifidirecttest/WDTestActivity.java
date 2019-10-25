package com.sjsu.boreas.wifidirecttest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.sjsu.boreas.R;

public class WDTestActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener {

    ScrollView actionContainer;
    LinearLayout peerListContainer, actionLog;
    TextView wifiStatus;

    Button discoverPeers, wifiGroup, peerActionToggle;
    boolean isGroupCreated = false, isPeerInfo = true;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    WDBroadcastReceiver receiver;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wdtest);

        actionContainer = (ScrollView) findViewById(R.id.wdtest_action_container);
        peerListContainer = (LinearLayout) findViewById(R.id.wdtest_peer_list_container);
        actionLog = (LinearLayout) findViewById(R.id.wdtest_action_log);
        wifiStatus = (TextView) findViewById(R.id.wdtest_connection_status);

        discoverPeers = (Button) findViewById(R.id.wdtest_button_discover);
        wifiGroup = (Button) findViewById(R.id.wdtest_button_wifigroup);
        peerActionToggle = (Button) findViewById(R.id.wdtest_button_peermode);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);

        addAction("Initializing WiFi Direct...");

        initWifiDirect();

    }

    public void initWifiDirect(){
        filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WDBroadcastReceiver(manager, channel, this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void onWifiStateChange(Intent intent){
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            wifiStatus.setText("WiFi Enabled");
            addAction("WiFi enabled");
        }
        else {
            wifiStatus.setText("WiFi Disabled");
            addAction("WiFi disabled");
        }
        wifiStatus.setText(intent.getAction());
    }

    public void onPeerChange(Intent intent){
        System.out.println(intent);
        addAction("Received peer change!");
        manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                String peerNames = "";
                addAction("Peers found: "+wifiP2pDeviceList.getDeviceList().size());
                peerListContainer.removeAllViews();
                Iterator<WifiP2pDevice> deviceIterator = wifiP2pDeviceList.getDeviceList().iterator();
                while(deviceIterator.hasNext()){
                    TextView peer = new TextView(getApplicationContext());
                    final WifiP2pDevice dev = deviceIterator.next();
                    peer.setText(dev.deviceName+": "+dev.deviceAddress+"\n"+dev.status+"    "+dev.primaryDeviceType);
                    peer.setTextColor(getResources().getColor(R.color.colorText));
                    peer.setClickable(true);
                    peer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = dev.deviceAddress;
                            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    addAction("Connected to "+dev.deviceAddress+" successfully!");
                                }

                                @Override
                                public void onFailure(int i) {
                                    addAction("Failed to connect to "+dev.deviceAddress);
                                }
                            });
                        }
                    });
                    peerListContainer.addView(peer);
                    peerNames += "-"+dev.deviceAddress+"\n";
                }
                addAction("Peers Found: "+peerNames);
            }
        });
    }

    public void onConnectionChange(Intent intent){
        System.out.println(intent);
       addAction(intent.getAction());
        //wifiStatus.setText(intent.getAction());
    }

    public void onDeviceWifiStateChange(Intent intent){
        System.out.println(intent);
        addAction(intent.getAction());
        //wifiStatus.setText(intent.getAction());
    }

    @Override
    public void onChannelDisconnected() {
        System.out.println("Channel Disconnect!");
        addAction("Channel Disconnected");
    }


    //Button Actions
    public void beginPeerDiscovery(View button){
        addAction("Beginning peer discovery");
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                addAction("Discover Peer Success");

            }

            @Override
            public void onFailure(int i) {
                addAction("Discover Peer Failure");
            }
        });
    }

    public void createGroup(View button){
        if(!isGroupCreated) {
            addAction("Creating group");
            wifiGroup.setText("Disband Group");
            isGroupCreated = true;
            manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    addAction("Created Group successfully");
                }

                @Override
                public void onFailure(int i) {
                    addAction("Couldn't create group");
                }
            });
        }else{
            addAction("Disbanding group");
            wifiGroup.setText("Create Group");
            isGroupCreated = false;
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    addAction("Successfully disbanded group");
                }

                @Override
                public void onFailure(int i) {
                    addAction("Failed to disband group");
                }
            });
        }
    }

    public void togglePeerAction(View button){
        if(isPeerInfo){
            peerActionToggle.setText("Add Peer");
            isPeerInfo = false;
        }else{
            peerActionToggle.setText("Peer Info");
            isPeerInfo = true;
        }
    }

    //Helper methods
    public void addAction(String act){

        LinearLayout log = new LinearLayout(getApplicationContext());
        log.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        log.setOrientation(LinearLayout.HORIZONTAL);

        Date date = Calendar.getInstance().getTime();
        String timestamp = DateFormat.getTimeInstance(DateFormat.LONG).format(date)+"\n";

        TextView message = new TextView(getApplicationContext());
        message.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        message.setTextColor(getResources().getColor(R.color.colorText));
        message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        message.setText(timestamp+act+"\n");



        /*
        TextView timestamp = new TextView(getApplicationContext());
        timestamp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        timestamp.setText();

         */

        log.addView(message);
        actionLog.addView(log);
        actionContainer.post(new Runnable() {
            @Override
            public void run() {
                actionContainer.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
