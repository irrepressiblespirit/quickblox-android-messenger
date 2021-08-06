package com.quickblox.sample.chat.java.repository;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.quickblox.sample.chat.java.repository.models.Dialog;
import com.quickblox.sample.chat.java.repository.models.User;

@Database(entities = {User.class, Dialog.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserRepository userRepository();
    public abstract DialogRepository dialogRepository();
}
