package org.rdtoolkit.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.rdtoolkit.support.model.session.ClassifierMode
import org.rdtoolkit.support.model.session.ProvisionMode
import org.rdtoolkit.support.model.session.STATUS
import org.rdtoolkit.support.model.session.SessionMode
import java.lang.reflect.Type
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromStatusEnum(value: STATUS?): String? { return value?.let { it.toString() } }

    @TypeConverter
    fun stringToStatus(status: String?): STATUS? { return status?.let{ STATUS.valueOf(status)} }

    @TypeConverter
    fun fromSessionModeEnum(value: SessionMode): String? { return value.toString() }

    @TypeConverter
    fun stringToSessionMode(value: String): SessionMode { return SessionMode.valueOf(value) }

    @TypeConverter
    fun fromProvisionMode(value: ProvisionMode): String? { return value.toString() }

    @TypeConverter
    fun stringToProvisionMode(value: String): ProvisionMode { return ProvisionMode.valueOf(value) }

    @TypeConverter
    fun fromClassifierMode(value: ClassifierMode): String? { return value.toString() }

    @TypeConverter
    fun stringToClassiferMode(value: String): ClassifierMode { return ClassifierMode.valueOf(value) }

    @TypeConverter
    fun stringToStringMap(value: String?): Map<String?, String?>? {
        val mapType: Type = object : TypeToken<Map<String?, String?>?>() {}.getType()
        return Gson().fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringMap(map: Map<String?, String?>?): String? {
        val gson = Gson()
        return gson.toJson(map)
    }
}
