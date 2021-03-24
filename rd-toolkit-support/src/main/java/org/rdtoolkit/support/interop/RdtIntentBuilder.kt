package org.rdtoolkit.support.interop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.rdtoolkit.support.model.session.*

abstract open class RdtIntentBuilder<T : RdtIntentBuilder<T>>() {

    protected var intent = Intent()
    protected var configBundle = Bundle()

    fun setSessionId(sessionId : String) : T {
        intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID, sessionId)
        return this as T
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

        @JvmStatic
        fun forProvisioning() : RdtProvisioningIntentBuilder  {
            val toReturn = RdtProvisioningIntentBuilder()
            toReturn.intent.action = ACTION_TEST_PROVISION
            toReturn.configBundle.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.TWO_PHASE.name)
            return toReturn
        }

        @JvmStatic
        fun forCapture() : CaptureIntentBuilder  {
            val toReturn = CaptureIntentBuilder()
            toReturn.intent.action = ACTION_TEST_CAPTURE
            return toReturn
        }

        @JvmStatic
        fun forProvisionAndCapture() : RdtProvisioningIntentBuilder  {
            val toReturn = RdtProvisioningIntentBuilder()
            toReturn.intent.action = ACTION_TEST_PROVISION_AND_CAPTURE
            toReturn.configBundle.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.ONE_PHASE.name)
            return toReturn
        }
    }
}

class CaptureIntentBuilder() : RdtIntentBuilder<CaptureIntentBuilder>() {

}


class RdtProvisioningIntentBuilder() : RdtIntentBuilder<RdtProvisioningIntentBuilder>() {

    /**
     * Request a session for a specific, single RDT, rather than letting the user choose one
     */
    fun requestTestProfile(testProfile : String) : RdtProvisioningIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE, ProvisionMode.TEST_PROFILE.name)
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, testProfile)
        return this
    }

    /**
     * Request an RDT which meets provided tag criteria, for example, a list of required
     * diagnostic outcomes, or a list of possible tests to choose from
     */
    fun requestProfileCriteria(tags : String, mode: ProvisionMode) : RdtProvisioningIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE, mode.name)
        configBundle.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, tags)
        return this
    }

    /**
     * Specify how (if at all) the output of available image classifiers should be made available
     * to the user.
     */
    fun setClassifierBehavior(mode: ClassifierMode) : RdtProvisioningIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_CLASSIFIER_MODE, mode.name)
        return this
    }

    /**
     * Set the first "flavor" text shown to the user to differentiate running tests
     */
    fun setFlavorOne(flavorText : String)  : RdtProvisioningIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE, flavorText)
        return this
    }

    /**
     * Set the second "flavor" text shown to the user to differentiate running tests
     */
    fun setFlavorTwo(flavorText : String) : RdtProvisioningIntentBuilder  {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO, flavorText)
        return this
    }

    /**
     * Specify a package id to be launched when a user chooses a test notification
     */
    fun setCallingPackage(packageId : String) : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_CALLING_PACKAGE, packageId)
        return this
    }

    /**
     * Directs the user back to this applicaiton when a user chooses a test notification
     */
    fun setReturnApplication(context : Context) : RdtProvisioningIntentBuilder {
        return setCallingPackage(context.packageName)
    }

    /**
     * Force users to not be able to provide a test result if the validity window for the test has
     * expired
     */
    fun setHardExpiration() : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SESSION_NO_EXPIRATION_OVERRIDE, FLAG_VALUE_SET);
        return this
    }

    /**
     * Prevent users from overriding the timer and reading results which may be early.
     */
    fun disableEarlyReads() : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SESSION_NO_EARLY_READS, FLAG_VALUE_SET);
        return this
    }

    /**
     * Apply a result response translator before returning the session intent
     */
    fun setResultResponseTranslator(responseTranslatorId : String) : RdtProvisioningIntentBuilder {
        configBundle.putString(INTENT_EXTRA_OUTPUT_RESULT_TRANSLATOR, responseTranslatorId)
        return this
    }

    fun setCloudworksBackend(cloudworksDns : String, cloudworksContext : String? = null ) : RdtProvisioningIntentBuilder {
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS, cloudworksDns)
        configBundle.putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_CONTEXT, cloudworksContext)
        return this
    }

    fun setSubmitAllImagesToCloudworks(captureAll : Boolean) : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SESSION_CLOUDWORKS_FULL_IMAGE_SUBMISSION, if (captureAll) FLAG_VALUE_SET else FLAG_VALUE_UNSET)
        return this
    }

    fun setCloudworksTraceEnabled(traceEnabled : Boolean) : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SESSION_CLOUDWORKS_CAPTURE_TRACE, if (traceEnabled) FLAG_VALUE_SET else FLAG_VALUE_UNSET)
        return this
    }

    /**
     * Control whether users can provide an 'indeterminate' result interpretation for tests
     */
    fun setIndeterminateResultsAllowed(allowed : Boolean) : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_CAPTURE_ALLOW_INDETERMINATE, if (allowed) FLAG_VALUE_SET else FLAG_VALUE_UNSET)
        return this
    }

    /**
     * Configure the test session in a testing / qa mode which will allow for overriding certain
     * actions for smoothness
     */
    fun setInTestQaMode() : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SESSION_TESTING_QA, FLAG_VALUE_SET)
        return this
    }

    fun setSecondaryCaptureRequirements(requirements : String) : RdtProvisioningIntentBuilder {
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SECONDARY_CAPTURE, FLAG_VALUE_SET)
        configBundle.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)!!.putString(FLAG_SECONDARY_PARAMS, requirements)
        return this
    }
}