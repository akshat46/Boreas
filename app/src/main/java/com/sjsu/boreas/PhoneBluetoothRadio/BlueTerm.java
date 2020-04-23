package com.sjsu.boreas.PhoneBluetoothRadio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SubMenu;

import com.sjsu.boreas.ChatViewRelatedStuff.ChatBubble;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.database.Messages.ChatMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class BlueTerm{

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-----Blueterm";

    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static BluetoothSerialService mSerialService;
    private static BluetoothDevice default_device_pi;

    private static String default_bluetooth_device_name = "raspberrypi";

    // Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public BlueTerm() {
        Log.e(TAG, SUB_TAG+ "+++ Constructor+++");
    }

    private static BluetoothDevice getBluetoothDeviceWithName(String name){
        BluetoothDevice temp = null;

        Log.e(TAG, SUB_TAG+"get blue tooth device: " + name);
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            Log.e(TAG, "\n  Device: " + device.getName() + ", " + device.getAddress());
            if(device.getName().equals(name)) {
                temp = device;
            }
        }

        Log.e(TAG, SUB_TAG+temp.getName());
        return temp;
    }

    //This function connects to whatever device name which is in default_bluetooth_device_name variable
    private static void connectToBluetoothDevice(){
        Log.e(TAG, SUB_TAG+"connected to blth function");
        if (mBluetoothAdapter == null) {
            Log.e(TAG, SUB_TAG+"This device doesn't have bluetooth.");
            return;
        }

        default_device_pi = getBluetoothDeviceWithName(default_bluetooth_device_name);
        mSerialService = new BluetoothSerialService();

        Log.e(TAG, "+++ DONE IN ON CREATE +++");

        //Get the rasperry pi

        mSerialService.connect(default_device_pi);
    }

    //This function is the one that sends the data out
    private static void send(final byte[] out) throws UnsupportedEncodingException {
        Log.e(TAG, SUB_TAG+"Sending mssg");

        if(mSerialService == null || mSerialService.getState() != BluetoothSerialService.STATE_CONNECTED){
            Log.e(TAG, SUB_TAG+"The device isn't connected to anything yet, gonna try to change that and connect to: " + default_bluetooth_device_name);
            connectToBluetoothDevice();
        }

        Log.e(TAG, SUB_TAG+"Serial state is: "+ mSerialService.getState());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, SUB_TAG+"Send the data inside this thread");
                while(mSerialService != null && mSerialService.getState() != BluetoothSerialService.STATE_CONNECTED) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (out.length > 0) {
                    try {
                        mSerialService.write(out);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //Change device name
    public static void connectToSpecificDevice(String name){
        default_bluetooth_device_name = name;
        connectToBluetoothDevice();
    }

    //This function is the function which will b used by outside classes to send stuff using radio
    //  it receives a mssg (ChatMessage converted into string) and a mssg type header which is useful for the pi
    //  to understand what the pi should do with this mssg
    public static void sendMessage(String mssg, int mssgType) {
        Log.e(TAG, SUB_TAG+"getting ready to send mssg");

        if(mssg == null || mssg.isEmpty())
            mssg = "BOREAS";

        String mssgToBeSent = String.valueOf(mssgType) + "-\n" + mssg;
        byte[] bytesToSend = mssgToBeSent.getBytes(Charset.forName("UTF-8"));

        try {
            send(bytesToSend);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, SUB_TAG+"There was an issue sending the mssg hea");
            e.printStackTrace();
        }
    }
}
