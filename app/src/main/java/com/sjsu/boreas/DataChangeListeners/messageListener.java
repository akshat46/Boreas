package com.sjsu.boreas.DataChangeListeners;

import com.sjsu.boreas.Database.Messages.ChatMessage;

public interface messageListener {
    public void newMessageReceived(ChatMessage mssg);
    public String getChatPartnerID();
}
