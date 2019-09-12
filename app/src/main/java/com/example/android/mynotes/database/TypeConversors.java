package com.example.android.mynotes.database;

import java.util.Date;

import androidx.room.TypeConverter;

public class TypeConversors {

    @TypeConverter
    public static Date toDate(Long time) {
        return time == null ? null : new Date(time);
    }

    @TypeConverter
    public static Long toTimeStamp(Date date) {
        return date == null ? null : date.getTime();
    }

}
