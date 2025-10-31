package com.team21.myapplication.data.local.converters

import androidx.room.TypeConverter
import com.google.firebase.Timestamp

class TimestampConverter {
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long? {
        return timestamp?.toDate()?.time
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(it / 1000, ((it % 1000) * 1000000).toInt()) }
    }
}
