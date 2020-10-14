package org.rdtoolkit.support.interop

import android.content.Intent
import android.os.Bundle
import org.rdtoolkit.support.model.session.*

class RdtIntentBuilder() {

    private var intent = Intent()
    private var configBundle = Bundle()

    fun forProvisioning() : RdtIntentBuilder  {
        intent.action = ACTION_TEST_PROVISION
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.TWO_PHASE.name)
        return this
    }

    fun forCapture() : RdtIntentBuilder  {
        intent.action = ACTION_TEST_CAPTURE
        return this
    }

    fun forProvisionAndCapture() : RdtIntentBuilder  {
        intent.action = ACTION_TEST_PROVISION_AND_CAPTURE
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.ONE_PHASE.name)
        return this
    }

    fun setClassifierBehavior(mode: ClassifierMode) {
        configBundle.putString(INTENT_EXTRA_RDT_CLASSIFIER_MODE, mode.name)
    }

    fun requestTestProfile(testProfile : String) : RdtIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE, ProvisionMode.TEST_PROFILE.name)
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, testProfile)
        return this
    }

    fun requestProfileCriteria(tags : String, mode: ProvisionMode) : RdtIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE, mode.name)
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, tags)
        return this
    }

    fun setFlavorOne(flavorText : String)  : RdtIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE, flavorText)
        return this
    }

    fun setFlavorTwo(flavorText : String) : RdtIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO, flavorText)
        return this
    }

    fun setResultResponseTranslator(responseTranslatorId : String) : RdtIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_OUTPUT_RESULT_TRANSLATOR, responseTranslatorId)
        return this
    }

    fun setSessionId(sessionId : String) : RdtIntentBuilder {
        intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID,sessionId)
        return this
    }

    fun setCallingPackage(packageId : String) : RdtIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_CALLING_PACKAGE, packageId)
        return this
    }

    fun setHardExpiration() : RdtIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SESSION_NO_EXPIRATION_OVERRIDE, FLAG_VALUE_SET);
        return this
    }

    init {
        configBundle.putBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS, Bundle())
        configBundle.putString(INTENT_EXTRA_RDT_CLASSIFIER_MODE, ClassifierMode.PRE_POPULATE.toString())
    }

    fun build(): Intent {
        intent.putExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE, configBundle);
        return intent
    }
    companion object {
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
    }
}
