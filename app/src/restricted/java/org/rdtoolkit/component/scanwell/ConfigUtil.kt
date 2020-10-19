package org.rdtoolkit.component.scanwell

import com.google.gson.Gson
import com.scanwell.rdtr.model.RdtMeasurements
import org.json.JSONObject

fun getMeasurementConfigs(json : JSONObject) : Map<String, ScanwellConfigData> {
    val gson = Gson()
    val returnMap = HashMap<String, ScanwellConfigData>()
    for (key in json.keys()) {
        val config = gson.fromJson(json[key].toString(), ScanwellConfigData::class.java)
        config.measurements!!
        config.responses!!
        returnMap.put(key, config)
    }
    return returnMap
}

class ScanwellConfigData(val measurements: RdtMeasurements, val responses : Map<String, Map<String, String>>)