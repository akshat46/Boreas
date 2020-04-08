package com.sjsu.boreas.PhoneBluetoothRadio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Set;


public class BlueTerm{

    public static String TAG = "-----Blueterm";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSerialService mSerialService;
    private BluetoothDevice default_device_pi;

    private String default_bluetooth_device_name = "raspberrypi";

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

        Log.e(TAG, "+++ ON CREATE +++");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "This device doesn't have bluetooth.");
            return;
        }

        default_device_pi = getBluetoothDeviceWithName(default_bluetooth_device_name);
        mSerialService = new BluetoothSerialService();

        Log.e(TAG, "+++ DONE IN ON CREATE +++");

        //Get the rasperry pi

        mSerialService.connect(default_device_pi);
    }

    public BluetoothDevice getBluetoothDeviceWithName(String name){
        BluetoothDevice temp = null;

        Log.e(TAG, "get blue tooth device: " + name);
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            Log.e(TAG, "\n  Device: " + device.getName() + ", " + device.getAddress());
            if(device.getName().equals(name)) {
                temp = device;
            }
        }

        Log.e(TAG, temp.getName());
        return temp;
    }

    public void send(byte[] out) throws UnsupportedEncodingException {
        Log.e(TAG, "Sending mssg");
//        if ( out.length == 1 ) {
//
//            if ( out[0] == 0x0D ) {
//                out = handleEndOfLineChars( mOutgoingEoL_0D );
//            }
//            else {
//                if ( out[0] == 0x0A ) {
//                    out = handleEndOfLineChars( mOutgoingEoL_0A );
//                }
//            }
//        }

        if ( out.length > 0 ) {
            mSerialService.write( out );
        }
    }
}
