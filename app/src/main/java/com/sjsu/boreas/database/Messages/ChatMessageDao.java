package com.sjsu.boreas.database.Messages;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.sjsu.boreas.database.Messages.ChatMessage;
import com.sjsu.boreas.database.User;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM chatMessage " +
            "WHERE receiverId is :chatPartnerID OR senderId is :chatPartnerID AND mssgType is :mssgType " +
            "ORDER BY time " +
            "ASC LIMIT 20")
    List<ChatMessage> getLastTwentyMessagesForUser(String chatPartnerID, int mssgType);

    @Insert
    void insertAll(ChatMessage... chatMessages);


}
