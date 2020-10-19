package org.rdtoolkit.component.scanwell

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.scanwell.rdtr.model.RdtMeasurements
import org.json.JSONObject

fun getMeasurementConfigs(json : JSONObject) : Map<String, ScanwellConfigData> {
    val gson = Gson()
    val returnMap = HashMap<String, ScanwellConfigData>()
    for (key in json.keys()) {
        val jsonData = json[key].toString()
        val config = gson.fromJson(jsonData, ScanwellConfigData::class.java)
        config.measurements!!
        config.responses!!
        returnMap.put(key, config)
    }
    return returnMap
}

class ScanwellConfigData(
        @SerializedName("measurements")
        val measurements: RdtMeasurements,
        @SerializedName("responses")
        val responses : Map<String, Map<String, String>>)
