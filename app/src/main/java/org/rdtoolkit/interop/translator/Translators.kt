package org.rdtoolkit.interop.translator

import android.content.Intent
import android.os.Bundle
import org.rdtoolkit.model.Mapper
import org.rdtoolkit.model.chain

fun getBootstrappedTranslators() : Map<String, Mapper<Intent, Intent>> {
    val translators = HashMap<String, Mapper<Intent, Intent>>()
    translators.put("xform_response", FlatIntentMapper().chain(XFormsResponseIntentMapper()))
    return translators
}

class XFormsResponseIntentMapper : Mapper<Intent, Intent> {
    override fun map(input: Intent): Intent {
        var response = Intent()
        response.putExtra("odk_intent_bundle", input.extras)
        return response
    }
}

/**
 * Take nested bundles and flatten any unambigious elements
 */
class FlatIntentMapper : Mapper<Intent, Intent> {
    override fun map(input: Intent): Intent {
        var response = Intent()
        aggregateKeysRecursive(response, input.extras!!)
        return response
    }

    private fun aggregateKeysRecursive(accumulator : Intent, current : Bundle) {
        for (key in current.keySet()) {
            val value = current.get(key)
            accumulator.putExtra(key, value?.let{flatten(value)})
        }
    }
}

fun flatten(input: Any) : String {
    when(input) {
        input is String -> return input as String
        input is Long -> return input.toString()
        input is Int -> return input.toString()
        else -> throw TODO("Implement converter for: " + input.javaClass)
    }
}