package org.rdtoolkit.support.interop

import android.content.Intent
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_BUNDLE
import org.rdtoolkit.support.model.session.TestSession

fun Intent.getRdtSession() : TestSession? {
    return RdtUtils.getRdtSession(this)
}

object RdtUtils {
    @JvmStatic
    fun getRdtSession(intent : Intent) : TestSession? {
        intent.getBundleExtra(INTENT_EXTRA_RDT_SESSION_BUNDLE)?.let {
            return BundleToSession().map(it)
        }
        return null
    }
}