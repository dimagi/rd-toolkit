package org.rdtoolkit.model

import androidx.room.TypeConverter
import org.rdtoolkit.model.session.STATUS
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
    fun fromStatusEnum(value: STATUS?): String? {
        return value?.let { it.toString() }
    }

    @TypeConverter
    fun stringToStatus(status: String?): STATUS? {
        return status?.let{ STATUS.valueOf(status)}
    }

}
