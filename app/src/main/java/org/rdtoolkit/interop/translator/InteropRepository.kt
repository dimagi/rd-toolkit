package org.rdtoolkit.interop.translator

import android.content.Intent
import org.rdtoolkit.interop.INTENT_EXTRA_RESPONSE_TRANSLATOR
import org.rdtoolkit.model.Mapper
import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.model.session.TestSessionDao
import java.util.HashMap

class InteropRepository() {
    private val builtInTranslators : Map<String, Mapper<Intent, Intent>> = getBootstrappedTranslators()

    fun getTranslator(id: String) : Mapper<Intent, Intent> {
        var mapper = builtInTranslators.get(id)
        if (mapper == null) {
            throw RuntimeException("No internal translator for: " + id)
        }
        return mapper
    }

    fun translateResponse(response : Intent) : Intent {
        if (!response.hasExtra(INTENT_EXTRA_RESPONSE_TRANSLATOR)) {
            return response
        }

        return getTranslator(response.getStringExtra(INTENT_EXTRA_RESPONSE_TRANSLATOR)!!)
                .map(response)
    }

    init {

    }
}