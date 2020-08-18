package org.rdtoolkit.interop

import android.content.Intent
import android.os.Bundle
import org.rdtoolkit.model.session.ProvisionMode
import org.rdtoolkit.model.session.SessionMode
import org.rdtoolkit.model.session.TestSession

const val ACTION_TEST_PROVISION = "org.rdtoolkit.action.Provision"
const val ACTION_TEST_PROVISION_AND_CAPTURE = "org.rdtoolkit.action.ProvisionAndCapture"
const val ACTION_TEST_CAPTURE = "org.rdtoolkit.action.Capture"

const val INTENT_EXTRA_RDT_SESSION_ID = "rdt_session_id"

const val INTENT_EXTRA_RDT_CONFIG_BUNDLE = "rdt_config_bundle"
const val INTENT_EXTRA_RDT_SESSION_BUNDLE = "rdt_session_bundle"

const val INTENT_EXTRA_INPUT_TRANSLATOR = "rdt_input_translate"
const val INTENT_EXTRA_OUTPUT_SESSION_TRANSLATOR = "rdt_output_session_translate"
const val INTENT_EXTRA_OUTPUT_RESULT_TRANSLATOR = "rdt_output_result_translate"

const val INTENT_EXTRA_RESPONSE_TRANSLATOR = "rdt_output_response_translate"

class TestIntentBuilder() {

    private var intent = Intent()
    private var configBundle = Bundle()

    fun forProvisioning() : TestIntentBuilder  {
        intent.action = ACTION_TEST_PROVISION
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.TWO_PHASE.name)
        return this
    }

    fun forCapture() : TestIntentBuilder  {
        intent.action = ACTION_TEST_CAPTURE
        return this
    }

    fun forProvisionAndCapture() : TestIntentBuilder  {
        intent.action = ACTION_TEST_PROVISION_AND_CAPTURE
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.ONE_PHASE.name)
        return this
    }

    fun requestTestProfile(testProfile : String) : TestIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE, ProvisionMode.TEST_PROFILE.name)
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, testProfile)
        return this
    }

    fun setFlavorOne(flavorText : String)  : TestIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE, flavorText)
        return this
    }

    fun setFlavorTwo(flavorText : String) : TestIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO, flavorText)
        return this
    }

    fun setSessionId(sessionId : String) : TestIntentBuilder {
        intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID,sessionId)
        return this
    }

    init {
        configBundle.putBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS, Bundle())
    }

    fun build(): Intent {
        intent.putExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE, configBundle);
        return intent
    }
}

fun bootstrap(newIntent: Intent, oldIntent: Intent) {
    newIntent.putExtra(INTENT_EXTRA_RDT_SESSION_ID,
            oldIntent.getStringExtra(INTENT_EXTRA_RDT_SESSION_ID))

    newIntent.putExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE,
            oldIntent.getBundleExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE))

}

fun provisionReturnIntent(session : TestSession) : Intent {
    val intent = Intent()
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_BUNDLE, SessionToBundle().map(session))
    return intent
}
