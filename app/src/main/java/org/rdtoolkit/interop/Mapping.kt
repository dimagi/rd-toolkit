package org.rdtoolkit.interop

import android.os.Bundle
import org.rdtoolkit.model.Mapper
import org.rdtoolkit.model.session.ProvisionMode
import org.rdtoolkit.model.session.STATUS
import org.rdtoolkit.model.session.SessionMode
import org.rdtoolkit.model.session.TestSession
import java.util.*
import kotlin.collections.HashMap

const val INTENT_EXTRA_RDT_PROVISION_MODE = "rdt_config_provision_mode"
const val INTENT_EXTRA_RDT_PROVISION_MODE_DATA = "rdt_config_provision_mode_data"
const val INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE = "rdt_config_flavor_one"
const val INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO = "rdt_config_flavor_two"
const val INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_SESSION = "rdt_config_translator_session"
const val INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_RESULT = "rdt_config_translator_result"

const val INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE = "rdt_config_session_type"
const val INTENT_EXTRA_RDT_CONFIG_FLAGS = "rdt_config_session_type"

const val INTENT_EXTRA_RDT_SESSION_STATE = "rdt_session_state"
const val INTENT_EXTRA_RDT_SESSION_TEST_PROFILE = "rdt_session_test_profile"
const val INTENT_EXTRA_RDT_SESSION_TIME_STARTED = "rdt_session_time_started"
const val INTENT_EXTRA_RDT_SESSION_TIME_RESOLVED = "rdt_session_time_resolved"
const val INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED = "rdt_session_time_expired"

class BundleToConfiguration : Mapper<Bundle, TestSession.Configuration> {
    override fun map(input: Bundle): TestSession.Configuration {
        val mapBundle = input.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)

        var map = HashMap<String,String>()

        if  (mapBundle != null) {
            for (k in mapBundle.keySet()) {
                map.put(k, mapBundle.getString(k)!!)
            }
        }

        return TestSession.Configuration(
                SessionMode.valueOf(input.getString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE)!!),
                ProvisionMode.valueOf(input.getString(INTENT_EXTRA_RDT_PROVISION_MODE)!!),
                input.getString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA)!!,
                input.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE),
                input.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO),
                input.getString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_SESSION),
                input.getString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_RESULT),
                map
        )
    }
}

class ConfigurationToBundle : Mapper<TestSession.Configuration, Bundle> {
    override fun map(input: TestSession.Configuration): Bundle {
        var b = Bundle()
        b.putString(INTENT_EXTRA_RDT_PROVISION_MODE, input.provisionMode.name)
        b.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, input.provisionModeData)
        b.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE, input.flavorText)
        b.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO, input.flavorTextTwo)
        b.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, input.sessionType.name)

        b.putString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_SESSION, input.outputSessionTranslatorId)
        b.putString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_RESULT, input.outputResultTranslatorId)

        var map = Bundle()
        for (k in input.flags) {
            map.putString(k.key, k.value)
        }
        b.putBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS, map)
        return b
    }
}

class SessionToBundle : Mapper<TestSession, Bundle> {
    override fun map(input: TestSession): Bundle {
        var b = Bundle()

        b.putString(INTENT_EXTRA_RDT_SESSION_ID, input.sessionId)
        b.putString(INTENT_EXTRA_RDT_SESSION_STATE, input.state.name)
        b.putString(INTENT_EXTRA_RDT_SESSION_TEST_PROFILE, input.testProfileId)
        b.putBundle(INTENT_EXTRA_RDT_CONFIG_BUNDLE, ConfigurationToBundle().map(input.configuration))
        b.putLong(INTENT_EXTRA_RDT_SESSION_TIME_STARTED, input.timeStarted.time)
        b.putLong(INTENT_EXTRA_RDT_SESSION_TIME_RESOLVED, input.timeResolved.time)
        b.putLong(INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED, input.timeExpired.time)

        return b
    }
}


/**
 * NOTE - Does NOT contain configuration information
 */
class BundleToSession : Mapper<Bundle, TestSession> {
    override fun map(input: Bundle): TestSession {
        return TestSession(
                input.getString(INTENT_EXTRA_RDT_SESSION_ID)!!,
                STATUS.valueOf(input.getString(INTENT_EXTRA_RDT_SESSION_STATE)!!),
                input.getString(INTENT_EXTRA_RDT_SESSION_TEST_PROFILE)!!,
                BundleToConfiguration().map(input.getBundle(INTENT_EXTRA_RDT_CONFIG_BUNDLE)!!),
                Date(input.getLong(INTENT_EXTRA_RDT_SESSION_TIME_STARTED)),
                Date(input.getLong(INTENT_EXTRA_RDT_SESSION_TIME_RESOLVED)),
                Date(input.getLong(INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED)),
                null
        )
    }
}