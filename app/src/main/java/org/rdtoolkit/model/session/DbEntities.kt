package org.rdtoolkit.model.session

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
/**
 * The record of an individual diagnostic test session requested and run
 */
data class DbTestSession (
        @PrimaryKey val sessionId: String,
        val state: STATUS,
        val testProfileId: String,
        val timeStarted: Date,
        val timeResolved : Date,
        val timeExpired : Date
) {
    fun getTestReadableState() : TestReadableState {
        if (timeStarted == null) {
            return TestReadableState.PREPARING
        } else if (Date().after(timeExpired)) {
            return TestReadableState.EXPIRED
        } else if (timeResolved.before(Date())) {
            return TestReadableState.READABLE;
        } else {
            return TestReadableState.RESOLVING;
        }
    }
}

@Entity
/**
 * The outcome and details of an individual diagnostic test
 */
data class DbTestSessionResult(
        @PrimaryKey val sessionId: String,
        var timeRead: Date?,
        var rawCapturedImageFilePath: String?,
        val results: MutableMap<String, String>
)


@Entity
/**
 * The configuration parameters for a diagnostic test session request
 */
data class DbTestSessionConfiguration(
        @PrimaryKey val sessionId: String,
        var sessionType: SessionMode,
        val provisionMode: ProvisionMode,
        val provisionModeData: String,
        val flavorText: String?,
        val flavorTextTwo: String?,
        val flags: Map<String, String>
)

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

data class DataTestSession (
        val session : DbTestSession,
        val config : DbTestSessionConfiguration,
        val result: DbTestSessionResult?
)