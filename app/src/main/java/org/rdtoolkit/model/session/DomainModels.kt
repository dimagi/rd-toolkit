package org.rdtoolkit.model.session

import java.util.*

data class TestSession (
        val sessionId: String,
        val state: STATUS,
        val testProfileId: String,
        val configuration: Configuration,
        val timeStarted : Date,
        val timeResolved : Date,
        val timeExpired : Date,
        var result: TestResult?
) {
    fun getTestReadableState() : TestReadableState {
        if (timeStarted == null) {
            return TestReadableState.PREPARING
        } else if (timeExpired != null && Date().after(timeExpired)) {
            return TestReadableState.EXPIRED
        } else if (timeResolved != null && timeResolved.before(Date())) {
            return TestReadableState.READABLE
        } else {
            return TestReadableState.RESOLVING
        }
    }

    data class TestResult(
            var timeRead: Date?,
            var rawCapturedImageFilePath: String?,
            val results: MutableMap<String, String>
    )

    data class Configuration(
            var sessionType: SessionMode,
            val provisionMode: ProvisionMode,
            var provisionModeData: String,
            val flavorText: String?,
            val flavorTextTwo: String?,
            val flags: Map<String, String>
    )
}

enum class SessionMode {
    /** Provision a test result, return the session id, then retrive the result later **/
    TWO_PHASE,
    /** Provision, then wait for a test and return the result without a break **/
    ONE_PHASE,
}

enum class ProvisionMode {
    /** Provide a specific test which should be provisioned **/
    TEST_PROFILE,

    /** Provide a diagnostic result to be evaluated, and allow the user to select a test which
     * can provide that result
     */
    RESULT_PROFILE,
}

data class TestProvision(
        val sessionId : String,
        val config: TestSession.Configuration) {}