package org.rdtoolkit.model.session

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class TestSession(
        @PrimaryKey val sessionId: String,
        val state: STATUS,
        val testProfileId: String,
        val flavorText: String,
        val flavorTextTwo: String,
        val timeStarted: Date,
        val timeResolved : Date,
        val timeExpired : Date,
        val timeRead: Date?
) {
    fun getTestReadableState() : TestReadableState {
        if (timeStarted == null) {
            return TestReadableState.PREPARING
        } else if (timeExpired != null && Date().after(timeExpired)) {
            return TestReadableState.EXPIRED
        } else if (timeResolved.before(Date())) {
            return TestReadableState.READABLE;
        } else {
            return TestReadableState.RESOLVING;
        }
    }
}

enum class TestReadableState {
    LOADING,
    PREPARING,
    RESOLVING,
    READABLE,
    EXPIRED
}

enum class STATUS {
    BUILDING, RUNNING, COMPLETE
}