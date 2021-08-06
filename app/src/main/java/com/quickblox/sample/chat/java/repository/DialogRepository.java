package com.quickblox.sample.chat.java.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.quickblox.sample.chat.java.repository.models.Dialog;

import java.util.List;
import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public interface DialogRepository {

    @Insert
    Single<Long> putDialog(Dialog dialog);

    @Query("SELECT * FROM Dialog WHERE id LIKE :dialogId")
    Maybe<List<Dialog>> getChatDialogById(String dialogId);

    @Query("SELECT * FROM dialog WHERE id IN (:ids)")
    Maybe<List<Dialog>> getChatDialogsByIds(List<String> ids);

    @Query("SELECT * FROM Dialog")
    Maybe<List<Dialog>> getDialogs();

    @Insert
    Single<List<Long>> putDialogs(List<Dialog> dialogList);

    @Delete
    Single<Integer> deleteDialog(Dialog dialog);

    @Delete
    Single<Integer> deleteDialogs(Dialog... dialogs);

    @Query("DELETE FROM  Dialog WHERE id LIKE :dialogId ")
    Single<Integer>  deleteDialogById(String dialogId);

    @Query("DELETE FROM Dialog WHERE id IN (:ids)")
    Single<Integer> deleteDialogsByIds(List<String> ids);

    @Query("DELETE FROM Dialog")
    Single<Integer> clear();

    @Update
    Single<Integer> updateDialog(Dialog dialog);
}
