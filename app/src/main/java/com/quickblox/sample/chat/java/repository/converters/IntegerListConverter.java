package com.quickblox.sample.chat.java.repository.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class IntegerListConverter {
    @TypeConverter
    public String fromIntegerList(List<Integer> integers) {
        if (integers == null || integers.isEmpty()) {
            return null;
        }
        return new Gson().toJson(integers, new TypeToken<List<Integer>>(){}.getType());
    }

    @TypeConverter
    public List<Integer> toIntegerList(String str) {
        if (str == null) {
            return null;
        }
        return new Gson().fromJson(str, new TypeToken<List<Integer>>(){}.getType());
    }
}
