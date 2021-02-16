package org.rdtoolkit.model.session

import androidx.annotation.NonNull
import androidx.room.*
import org.rdtoolkit.support.model.session.*
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
        val timeExpired : Date?
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
        var mainImage: String?,
        val images: MutableMap<String, String>,
        val results: MutableMap<String, String>,
        val classifierResults: MutableMap<String, String>
)

@Entity
/**
 * Metadata metrics information about test sessions
 */
data class DbTestSessionMetrics (
        @PrimaryKey val sessionId: String,
        val data: MutableMap<String, String>
)

@Entity(indices = [
    Index(name = "sessionId_index", value = ["sessionId"])
])
/**
 * Discrete, loggable events for forensics and monitoring of test sessions
 */
data class DbTestSessionTraceEvent (
        @PrimaryKey(autoGenerate = true)
        val id : Int,
        @NonNull
        val sessionId: String,
        val timestamp : String,
        val eventTag : String,
        val eventMessage : String,
        val eventJson : String?,
        val sandboxObjectId: String?
)

@Entity
/**
 * The configuration parameters for a diagnostic test session request
 */
data class DbTestSessionConfiguration(
        @PrimaryKey val sessionId: String,
        var sessionType: SessionMode,
        val provisionMode: ProvisionMode,
        val classifierMode: ClassifierMode,
        val provisionModeData: String,
        val flavorText: String?,
        val flavorTextTwo: String?,
        val outputSessionTranslatorId: String?,
        val outputResultTranslatorId: String?,
        val cloudworksDns: String?,
        val cloudworksContext: String?,
        val flags: Map<String, String>
)

data class DataTestSession (
        @Embedded
        val session : DbTestSession,
        @Relation(parentColumn = "sessionId", entityColumn = "sessionId")
        val config : DbTestSessionConfiguration,
        @Relation(parentColumn = "sessionId", entityColumn = "sessionId")
        val result: DbTestSessionResult?,
        @Relation(parentColumn = "sessionId", entityColumn = "sessionId")
        val metrics: DbTestSessionMetrics?
)