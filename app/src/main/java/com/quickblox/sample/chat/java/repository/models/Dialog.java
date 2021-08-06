package com.quickblox.sample.chat.java.repository.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.quickblox.sample.chat.java.repository.converters.IntegerListConverter;

import java.util.List;

import lombok.Data;

@Data
@Entity
public class Dialog {

    @PrimaryKey
    @NonNull
    public String id = "dialogId";

    @ColumnInfo
    public String name;

    @ColumnInfo(name = "last_message")
    public String lastMessage;

    @ColumnInfo(name = "last_message_date_sent")
    public long lastMessageDateSent;

    @ColumnInfo(name = "last_message_user_id")
    public int lastMessageUserId;

    @ColumnInfo
    public String photo;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "xmpp_room_jid")
    public String roomJid;

    @ColumnInfo(name = "unread_messages_count")
    public int unreadMessageCount;

    @ColumnInfo(name = "occupants_ids")
    @TypeConverters({IntegerListConverter.class})
    public List<Integer> occupantsIds;

    @ColumnInfo
    public int type;
}
