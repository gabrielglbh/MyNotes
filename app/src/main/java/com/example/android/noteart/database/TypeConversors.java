package com.example.android.noteart.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

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
