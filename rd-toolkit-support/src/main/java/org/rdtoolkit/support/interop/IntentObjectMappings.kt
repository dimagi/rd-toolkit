package org.rdtoolkit.support.interop

import android.os.Bundle
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_CONFIG_BUNDLE
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_ID
import org.rdtoolkit.support.model.Mapper
import org.rdtoolkit.support.model.session.*
import java.util.*
import kotlin.collections.HashMap

const val INTENT_EXTRA_RDT_PROVISION_MODE = "rdt_config_provision_mode"
const val INTENT_EXTRA_RDT_PROVISION_MODE_DATA = "rdt_config_provision_mode_data"
const val INTENT_EXTRA_RDT_CLASSIFIER_MODE = "rdt_config_classifier_mode"
const val INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE = "rdt_config_flavor_one"
const val INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO = "rdt_config_flavor_two"
const val INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_SESSION = "rdt_config_translator_session"
const val INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_RESULT = "rdt_config_translator_result"
const val INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS = "rdt_config_cloudworks_dns"
const val INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_CONTEXT = "rdt_config_cloudworks_context"

const val INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE = "rdt_config_session_type"
const val INTENT_EXTRA_RDT_CONFIG_FLAGS = "rdt_config_session_flags"

const val INTENT_EXTRA_RDT_SESSION_STATE = "rdt_session_state"
const val INTENT_EXTRA_RDT_SESSION_TEST_PROFILE = "rdt_session_test_profile"
const val INTENT_EXTRA_RDT_SESSION_TIME_STARTED = "rdt_session_time_started"
const val INTENT_EXTRA_RDT_SESSION_TIME_RESOLVED = "rdt_session_time_resolved"
const val INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED = "rdt_session_time_expired"

const val INTENT_EXTRA_RDT_RESULT_TIME_READ = "rdt_session_result_time_read"
const val INTENT_EXTRA_RDT_RESULT_RAW_IMAGE_PATH = "rdt_session_result_raw_image_path"
const val INTENT_EXTRA_RDT_RESULT_MAP = "rdt_session_result_map"
const val INTENT_EXTRA_RDT_RESULT_INTERPRETER_MAP = "rdt_session_result_interpreter_map"
const val INTENT_EXTRA_RDT_RESULT_IMAGES = "rdt_session_result_extra_images"

const val INTENT_EXTRA_RDT_RESULT_BUNDLE = "rdt_session_result_bundle"


class BundleToConfiguration : Mapper<Bundle, TestSession.Configuration> {
    override fun map(input: Bundle): TestSession.Configuration {
        val mapBundle = input.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)

        var map = BundleToStringMap().map(mapBundle)

        return TestSession.Configuration(
                SessionMode.valueOf(input.getString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE)!!),
                ProvisionMode.valueOf(input.getString(INTENT_EXTRA_RDT_PROVISION_MODE)!!),
                ClassifierMode.valueOf(input.getString(INTENT_EXTRA_RDT_CLASSIFIER_MODE)!!),
                input.getString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA)!!,
                input.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE),
                input.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO),
                input.getString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_SESSION),
                input.getString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_RESULT),
                input.getString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS),
                input.getString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_CONTEXT),
                map
        )
    }
}

class ConfigurationToBundle : Mapper<TestSession.Configuration, Bundle> {
    override fun map(input: TestSession.Configuration): Bundle {
        var b = Bundle()
        b.putString(INTENT_EXTRA_RDT_PROVISION_MODE, input.provisionMode.name)
        b.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, input.provisionModeData)
        b.putString(INTENT_EXTRA_RDT_CLASSIFIER_MODE, input.classifierMode.name)
        b.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE, input.flavorText)
        b.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO, input.flavorTextTwo)
        b.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, input.sessionType.name)

        b.putString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_SESSION, input.outputSessionTranslatorId)
        b.putString(INTENT_EXTRA_RDT_CONFIG_OUTPUT_TRANSLATOR_RESULT, input.outputResultTranslatorId)

        b.putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS, input.cloudworksDns)
        b.putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_CONTEXT, input.cloudworksContext)

        b.putBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS, StringMapToBundle().map(input.flags))
        return b
    }
}

class BundleToResult : Mapper<Bundle, TestSession.TestResult> {
    override fun map(input: Bundle): TestSession.TestResult {
        val mapBundle = input.getBundle(INTENT_EXTRA_RDT_RESULT_MAP)
        val interpreterBundle = input.getBundle(INTENT_EXTRA_RDT_RESULT_INTERPRETER_MAP)
        val extraImages = input.getBundle(INTENT_EXTRA_RDT_RESULT_IMAGES)

        return TestSession.TestResult(
                Date(input.getLong(INTENT_EXTRA_RDT_RESULT_TIME_READ)),
                input.getString(INTENT_EXTRA_RDT_RESULT_RAW_IMAGE_PATH),
                getMapFromBundle(extraImages),
                getMapFromBundle(mapBundle),
                getMapFromBundle(interpreterBundle)
        )
    }

    fun getMapFromBundle(bundle : Bundle?) : HashMap<String,String> {
        var map = HashMap<String,String>()

        if  (bundle != null) {
            for (k in bundle.keySet()) {
                map.put(k, bundle.getString(k)!!)
            }
        }
        return map
    }
}

class ResultToBundle : Mapper<TestSession.TestResult, Bundle> {
    override fun map(input: TestSession.TestResult): Bundle {
        var b = Bundle()
        b.putLong(INTENT_EXTRA_RDT_RESULT_TIME_READ, input.timeRead!!.time)
        b.putString(INTENT_EXTRA_RDT_RESULT_RAW_IMAGE_PATH, input.mainImage)

        b.putBundle(INTENT_EXTRA_RDT_RESULT_IMAGES, getBundleFromMap(input.images))

        b.putBundle(INTENT_EXTRA_RDT_RESULT_MAP, getBundleFromMap(input.results))
        b.putBundle(INTENT_EXTRA_RDT_RESULT_INTERPRETER_MAP, getBundleFromMap(input.classifierResults))
        return b
    }

    fun getBundleFromMap(inputMap : Map<String, String>) : Bundle {
        var map = Bundle()
        for (k in inputMap) {
            map.putString(k.key, k.value)
        }
        return map
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
        input.timeExpired?.let {
            b.putLong(INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED, it.time)
        }
        input.result?.let{
            b.putBundle(INTENT_EXTRA_RDT_RESULT_BUNDLE, ResultToBundle().map(it))}

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
                input.get(INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED)?.let { Date(input.getLong(INTENT_EXTRA_RDT_SESSION_TIME_EXPIRED))},
                input.getBundle(INTENT_EXTRA_RDT_RESULT_BUNDLE)?.let { BundleToResult().map(it) }
        )
    }
}

class StringMapToBundle : Mapper<Map<String, String>, Bundle> {
    override fun map(input: Map<String, String>): Bundle {
        var map = Bundle()
        for (k in input) {
            map.putString(k.key, k.value)
        }
        return map
    }
}

interface KeyQualifier {
    fun isKeyQualified(key: String) : Boolean
}

class AllKeyQualifier() : KeyQualifier {
    override fun isKeyQualified(key: String): Boolean {
        return true
    }
}


class FixedSetKeyQualifier(val keys : Collection<String>) : KeyQualifier {
    override fun isKeyQualified(key: String): Boolean {
        return keys.contains(key)
    }
}

class PrefixKeyQualifier(val prefix : String) : KeyQualifier {
    override fun isKeyQualified(key: String): Boolean {
        return key.startsWith(prefix)
    }
}

class BundleToStringMap(private val qualifier : KeyQualifier = AllKeyQualifier())
    : Mapper<Bundle?, Map<String, String>> {

    override fun map(input: Bundle?): Map<String, String> {
        if (input == null) {
            return HashMap()
        }

        val map = HashMap<String,String>()

        if  (input != null) {
            for (k in input.keySet()) {
                if (qualifier.isKeyQualified(k)) {
                    map.put(k, input.getString(k)!!)
                }
            }
        }
        return map
    }

}