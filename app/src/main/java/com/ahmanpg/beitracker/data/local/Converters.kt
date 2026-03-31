package com.ahmanpg.beitracker.data.local

import androidx.room.TypeConverter
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTrackedItem(item: TrackedItem?): String? {
        return gson.toJson(item)
    }

    @TypeConverter
    fun toTrackedItem(json: String?): TrackedItem? {
        if (json == null) return null
        val type = object : TypeToken<TrackedItem>() {}.type
        return gson.fromJson(json, type)
    }
}
