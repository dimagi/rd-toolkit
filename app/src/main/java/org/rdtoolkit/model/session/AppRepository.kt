package org.rdtoolkit.model.session

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.rdtoolkit.support.interop.RdtIntentBuilder
import org.rdtoolkit.support.model.session.ProvisionMode
import java.util.*

class AppRepository(private val context : Context) {
    fun hasAcknowledgedDisclaimer() : Boolean {
        return prefs().getBoolean(PREFERENCE_ACKNOWLEDGED_DISCLAIMER, false)
    }

    fun setAcknowledgedDisclaimer(acknowledged : Boolean) {
        prefs().edit().putBoolean(PREFERENCE_ACKNOWLEDGED_DISCLAIMER, acknowledged).apply()
    }

    fun hasAcknowledgedEarlyTimerDisclaimer() : Boolean {
        return prefs().getBoolean(PREFERENCE_EARLY_TIMER_DISCLAIMER, false)
    }

    fun setAcknowledgedEarlyTimerDisclaimer(acknowledged : Boolean) {
        prefs().edit().putBoolean(PREFERENCE_EARLY_TIMER_DISCLAIMER, acknowledged).apply()
    }

    fun isNetworkRestrictedByBattery() : Boolean {
        return prefs().getBoolean(PREFERENCE_RESTRICT_NETWORK_BATTERY, false)
    }

    fun clearDisclaimers() {
        var editor = prefs().edit()
        editor.remove(PREFERENCE_ACKNOWLEDGED_DISCLAIMER)
        editor.remove(PREFERENCE_EARLY_TIMER_DISCLAIMER)
        editor.commit()
    }

    fun getDemoIntentBuilder() : RdtIntentBuilder<*> {
        return RdtIntentBuilder.forProvisioning().setSessionId(UUID.randomUUID().toString()) //.requestTestProfile("debug_mal_pf_pv")s
                //.requestTestProfile("sd_bioline_mal_pf_pv")
                .requestProfileCriteria(prefs().getString(PREFERENCE_KEY_DIAGNOSTIC,"mal_pf")!!, ProvisionMode.CRITERIA_SET_AND) //.requestProfileCriteria("sd_bioline_mal_pf_pv carestart_mal_pf_pv", ProvisionMode.CRITERIA_SET_OR)
                //.requestProfileCriteria("fake", ProvisionMode.CRITERIA_SET_OR)
                .setFlavorOne("Tedros Adhanom")
                .setFlavorTwo("#4SFS") //.setClassifierBehavior(ClassifierMode.CHECK_YOUR_WORK)
                .setInTestQaMode() //.setSecondaryCaptureRequirements("capture_windowed")
                //.setSubmitAllImagesToCloudworks(true)
                .setIndeterminateResultsAllowed(true)
    }

    private fun prefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)!!
    }

    companion object {
        const val PREFERENCE_ACKNOWLEDGED_DISCLAIMER = "user_acknwoledged_disclaimer"
        const val PREFERENCE_EARLY_TIMER_DISCLAIMER = "user_acknowleged_early_timer"
        const val PREFERENCE_RESTRICT_NETWORK_BATTERY = "restrict_network_battery"
        const val PREFERENCE_KEY_DIAGNOSTIC = "diagnostic_id"
    }
}