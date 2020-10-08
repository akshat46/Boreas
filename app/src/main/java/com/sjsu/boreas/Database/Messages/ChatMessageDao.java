package com.sjsu.boreas.Database.Messages;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM chatMessage " +
            "WHERE receiverId is :chatPartnerID OR senderId is :chatPartnerID AND mssgType is :mssgType " +
            "ORDER BY time " +
            "ASC LIMIT 20")
    List<ChatMessage> getLastTwentyMessagesForUser(String chatPartnerID, int mssgType);

    @Query("SELECT * FROM chatMessage " +
            "WHERE mssgId is :messageId")
    List<ChatMessage> getSpecificMessage(String messageId);

    @Query("SELECT * FROM chatmessage")
    List<ChatMessage> getAllMessages();

    @Insert
    void insertAll(ChatMessage... chatMessages);



}
