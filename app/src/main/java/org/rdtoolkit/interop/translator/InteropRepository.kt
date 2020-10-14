package org.rdtoolkit.interop.translator

import android.content.Intent
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RESPONSE_TRANSLATOR
import org.rdtoolkit.support.model.Mapper

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