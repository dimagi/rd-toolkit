package org.rdtoolkit.model.session

import android.content.Context
import android.content.SharedPreferences
import org.rdtoolkit.R

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

    fun clearDisclaimers() {
        var editor = prefs().edit()
        editor.remove(PREFERENCE_ACKNOWLEDGED_DISCLAIMER)
        editor.remove(PREFERENCE_EARLY_TIMER_DISCLAIMER)
        editor.commit()
    }

    private fun prefs(): SharedPreferences {
        return context.getSharedPreferences(context.getString(R.string.default_preference_key)
                , Context.MODE_PRIVATE)!!
    }

    companion object {
        const val PREFERENCE_ACKNOWLEDGED_DISCLAIMER = "user_acknwoledged_disclaimer"
        const val PREFERENCE_EARLY_TIMER_DISCLAIMER = "user_acknowleged_early_timer"


    }
}