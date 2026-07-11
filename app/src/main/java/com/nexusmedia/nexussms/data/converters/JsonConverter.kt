package com.nexusmedia.nexussms.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Centralized JSON <-> Kotlin conversion for Room fields.
 *
 * Note: current entities store many JSON-backed fields as `String`, but the tracker spec
 * expects a JSON TypeConverter to support richer model shapes later.
 */
class JsonConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromJsonListString(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return gson.fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun listStringToJson(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun fromJsonListInt(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return gson.fromJson(value, object : TypeToken<List<Int>>() {}.type)
    }

    @TypeConverter
    fun listIntToJson(value: List<Int>?): String {
        return gson.toJson(value ?: emptyList<Int>())
    }

    @TypeConverter
    fun fromJsonMapStringToListString(value: String?): Map<String, List<String>> {
        if (value.isNullOrBlank()) return emptyMap()
        return gson.fromJson(
            value,
            object : TypeToken<Map<String, List<String>>>() {}.type
        )
    }

    @TypeConverter
    fun mapStringToListStringToJson(value: Map<String, List<String>>?): String {
        return gson.toJson(value ?: emptyMap<String, List<String>>())
    }

    @TypeConverter
    fun fromJsonMapStringToString(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        return gson.fromJson(
            value,
            object : TypeToken<Map<String, String>>() {}.type
        )
    }

    @TypeConverter
    fun mapStringToStringToJson(value: Map<String, String>?): String {
        return gson.toJson(value ?: emptyMap<String, String>())
    }
}

