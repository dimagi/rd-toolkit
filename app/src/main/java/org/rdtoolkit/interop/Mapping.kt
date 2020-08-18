package org.rdtoolkit.interop

import android.os.Bundle
import org.rdtoolkit.model.Mapper
import org.rdtoolkit.model.session.ProvisionMode
import org.rdtoolkit.model.session.SessionMode
import org.rdtoolkit.model.session.TestSession

const val INTENT_EXTRA_RDT_PROVISION_MODE = "rdt_config_provision_mode"
const val INTENT_EXTRA_RDT_PROVISION_MODE_DATA = "rdt_config_provision_mode_data"
const val INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE = "rdt_config_flavor_one"
const val INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO = "rdt_config_flavor_two"

const val INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE = "rdt_config_session_type"
const val INTENT_EXTRA_RDT_CONFIG_FLAGS = "rdt_config_session_type"


class BundleToConfiguration : Mapper<Bundle, TestSession.Configuration> {
    override fun map(input: Bundle): TestSession.Configuration {
        val mapBundle = input.getBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS)

        var map = HashMap<String,String>()

        if  (mapBundle != null) {
            for (k in mapBundle.keySet()) {
                map.put(k, mapBundle.getString(k)!!)
            }
        }

        return TestSession.Configuration(
                SessionMode.valueOf(input.getString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE)!!),
                ProvisionMode.valueOf(input.getString(INTENT_EXTRA_RDT_PROVISION_MODE)!!),
                input.getString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA)!!,
                input.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE),
                input.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO),
                map
        )
    }
}

class ConfigurationToBundle : Mapper<TestSession.Configuration, Bundle> {
    override fun map(input: TestSession.Configuration): Bundle {
        var b = Bundle()
        b.putString(INTENT_EXTRA_RDT_PROVISION_MODE, input.provisionMode.name)
        b.putString(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, input.provisionModeData)
        b.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE, input.flavorText)
        b.putString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_TWO, input.flavorTextTwo)
        b.putString(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, input.sessionType.name)

        var map = Bundle()
        for (k in input.flags) {
            map.putString(k.key, k.value)
        }
        b.putBundle(INTENT_EXTRA_RDT_CONFIG_FLAGS, map)
        return b
    }

}