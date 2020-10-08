package org.rdtoolkit.interop.translator

import android.content.Intent
import android.os.Bundle
import org.rdtoolkit.interop.*
import org.rdtoolkit.model.Mapper
import org.rdtoolkit.model.chain
import org.rdtoolkit.model.session.TestSession
import java.util.*
import kotlin.collections.HashMap

const val TRANSLATOR_XFORM_RESULT ="xform_response"

const val TRANSLATOR_PROVISION_FLAT = "provision_flat"

fun getBootstrappedTranslators() : Map<String, Mapper<Intent, Intent>> {
    val translators = HashMap<String, Mapper<Intent, Intent>>()
    translators.put(TRANSLATOR_XFORM_RESULT, FlatIntentMapper(XFormDateFlatener()).chain(XFormsResponseIntentMapper()))

    translators.put(TRANSLATOR_PROVISION_FLAT,
    RollupIntentRemapper(BundleToStringMap(FixedSetKeyQualifier(setOf())), StringMapToBundle(), INTENT_EXTRA_RDT_CONFIG_FLAGS).chain(
            RollupIntentRemapper(BundleToConfiguration() as Mapper<Bundle?, TestSession.Configuration>, ConfigurationToBundle(), INTENT_EXTRA_RDT_CONFIG_BUNDLE, true)
    ))

    return translators
}

class XFormsResponseIntentMapper : Mapper<Intent, Intent> {
    override fun map(input: Intent): Intent {
        var response = Intent()
        response.putExtra("odk_intent_bundle", input.extras)
        response.putExtra("odk_intent_data", "Success")
        return response
    }
}

/**
 * Translates some portion of the intent into another object and then injects it into
 * a copy of the intent with a name
 */
class RollupIntentRemapper<T>(val inMapper: Mapper<Bundle?, T>,
                                       val outMapper: Mapper<T, Bundle>,
                                       val newKeyName: String,
                                       val cleanOutput : Boolean = false) : Mapper<Intent, Intent> {
    override fun map(input: Intent) : Intent {
        val output = Intent()

        if(!cleanOutput) {
            output.putExtras(input.extras!!)
        }
        output.putExtra(newKeyName, outMapper.map(inMapper.map(input.extras!!)))

        return output
    }
}

/**
 * Take nested bundles and flatten any unambigious elements into basic string/string
 * maps
 */
class FlatIntentMapper(val flattener : TypeFlattener = TypeFlattener()): Mapper<Intent, Intent> {
    override fun map(input: Intent): Intent {
        var response = Intent()
        aggregateKeysRecursive(response, input.extras!!)
        return response
    }

    private fun aggregateKeysRecursive(accumulator : Intent, current : Bundle) {
        for (key in current.keySet()) {
            val value = current.get(key)
            if (value is Bundle) {
                aggregateKeysRecursive(accumulator, value)
            } else {
                accumulator.putExtra(key, value?.let { flattener.flatten(key, value) })
            }
        }
    }
}

open class TypeFlattener {
    open fun flatten(key: String, input: Any) : String {
        when(input) {
            is String -> return input as String
            is Long -> return input.toString()
            is Int -> return input.toString()
            else -> throw TODO("Implement converter for: " + input.javaClass)
        }
    }
}

//TODO: This should probably use composition but it would add complexity around whether
//items are handled
class XFormDateFlatener : TypeFlattener() {
    override fun flatten(key: String, input: Any) : String {
        if (input is Long && (
                        key.contains("date", true) ||
                        key.contains("time", true))
        ){
            return (input + TimeZone.getDefault().getOffset(input)).toString()
        } else {
            return super.flatten(key, input)
        }
    }
}