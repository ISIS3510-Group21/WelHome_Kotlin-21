package com.team21.myapplication.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.team21.myapplication.ui.mapsearch.MapLocation

class MapLocationConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromMapLocationList(locations: List<MapLocation>): String {
        return gson.toJson(locations)
    }

    @TypeConverter
    fun toMapLocationList(locationsJson: String): List<MapLocation> {
        val listType = object : TypeToken<List<MapLocation>>() {}.type
        return gson.fromJson(locationsJson, listType)
    }
}
