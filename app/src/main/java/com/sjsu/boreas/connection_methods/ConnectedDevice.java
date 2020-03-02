package com.sjsu.boreas.connection_methods;

/**
 * Represents another android device that this user has connected to and can send messages to.
 */
public abstract class ConnectedDevice {

    public abstract void sendMessage(String text);
}
