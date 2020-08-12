package org.rdtoolkit.model.session

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class TestSession(
        @PrimaryKey val sessionId: String,
        val state: STATUS,
        val flavorText: String,
        val flavorTextTwo: String,
        val timeResolved : Date,
        val timeExpired : Date
)

enum class STATUS {
    BUILDING, RUNNING, COMPLETE
}