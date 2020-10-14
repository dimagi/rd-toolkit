package org.rdtoolkit.interop

import android.content.Context
import android.content.Intent
import org.rdtoolkit.interop.translator.InteropRepository
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_INPUT_TRANSLATOR
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_CONFIG_BUNDLE
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_BUNDLE
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_ID
import org.rdtoolkit.support.interop.SessionToBundle
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.ui.provision.ProvisionActivity

fun provisionIntent(context : Context, incoming: Intent) : Intent {
    var toBootstrap = incoming
    incoming.getStringExtra(INTENT_EXTRA_INPUT_TRANSLATOR)?.let {
        toBootstrap = InteropRepository().getTranslator(it).map(incoming)
        toBootstrap.putExtra(INTENT_EXTRA_RDT_SESSION_ID,
                incoming.getStringExtra(INTENT_EXTRA_RDT_SESSION_ID))
    }
    var toReturn = Intent(context, ProvisionActivity::class.java)
    bootstrap(toReturn, toBootstrap)

    return toReturn
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

fun captureReturnIntent(session : TestSession) : Intent {
    val intent = Intent()
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_BUNDLE, SessionToBundle().map(session))
    return intent
}
