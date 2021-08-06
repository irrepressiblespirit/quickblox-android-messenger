package com.quickblox.sample.chat.java.repository.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.quickblox.sample.chat.java.repository.converters.DateConverter;

import java.util.Date;

import lombok.Data;

@Data
@Entity
public class User {

    @PrimaryKey
    public int id;

    @ColumnInfo
    public String login;

    @ColumnInfo
    public String email;

    @ColumnInfo
    public String phone;

    @ColumnInfo
    public String website;

    @ColumnInfo(name = "full_name")
    public String fullName;

    @ColumnInfo(name = "last_request_at")
    @TypeConverters({DateConverter.class})
    public Date lastRequestAt;

    @ColumnInfo(name = "external_user_id")
    public String externalId;

    @ColumnInfo(name = "facebook_id")
    public String facebookId;

    @ColumnInfo(name = "twitter_id")
    public String twitterId;

    @ColumnInfo(name = "twitter_digits_id")
    public String twitterDigitsId;

    @ColumnInfo(name = "blob_id")
    public int blobId;

    @ColumnInfo(name = "user_tags")
    public String tags;

    @ColumnInfo
    public String password;

    @ColumnInfo
    public String oldPassword;

    @ColumnInfo(name = "custom_data")
    public String customData;

}
