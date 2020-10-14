package org.rdtoolkit.support.model.session

import java.util.*

data class TestSession (
        val sessionId: String,
        var state: STATUS,
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
            val results: MutableMap<String, String>,
            val classifierResults: MutableMap<String, String>
    )

    data class Configuration(
            var sessionType: SessionMode,
            val provisionMode: ProvisionMode,
            val classifierMode: ClassifierMode,
            var provisionModeData: String,
            val flavorText: String?,
            val flavorTextTwo: String?,
            val outputSessionTranslatorId: String?,
            val outputResultTranslatorId: String?,
            val flags: Map<String, String>
    )
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

enum class ClassifierMode {
    /**
     * No image classifier will be applied to the captured image, even if one is avaialble
     */
    NONE,
    /**
     * Users will not be notified of results. They will enter their own interpretation without
     * any prompting resulting from automated classifiers
     */
    BLIND,
    /**
     * After the classifier completes, it will pre-populate the results of the RDT, but the user
     * will have the ability to change the results suggested
     */
    PRE_POPULATE,
    /**
     * The user will not receive any specific feedback about the classifier's outputs unless the
     * classifier disagrees with a user selected input. If so, the classifier will notify the user
     * and suggest corrections, which the user can
     */
    CORRECTION,
    /**
     * The classifier will present the user with its interpretation of the test result without
     * allowing the user to override or suggest an alternative
     */
    AUTHORITY
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

    /** Provide a set of tags to filter available tests by exclusive matching criteria **/
    CRITERIA_SET_OR,

    /** Provide a set of tags to filter available tests by inclusive matching criteria **/
    CRITERIA_SET_AND,
}