package com.quickblox.sample.chat.java.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.quickblox.sample.chat.java.repository.models.User;

import java.util.List;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface UserRepository {

    @Insert
    Single<Long> putUser(User user);

    @Insert
    Single<List<Long>> putUsers(User... user);

    @Query("SELECT * FROM user WHERE id = :id")
    Maybe<List<User>> getUserById(int id);

    @Query("SELECT * FROM user WHERE id IN (:ids)")
    Maybe<List<User>> getUsersByIds(List<Integer> ids);

    @Query("SELECT * FROM user")
    Maybe<List<User>> getAllUsers();

    @Delete
    Single<Integer> deleteUser(User user);

    @Update
    Single<Integer> updateUser(User user);
}
