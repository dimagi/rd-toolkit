package org.rdtoolkit.interop

import org.json.JSONObject
import org.rdtoolkit.support.model.Mapper
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.support.model.session.TestSessionTraceEvent
import org.rdtoolkit.util.getIsoUTCTimestamp


class ConfigurationToJson(val stripSensitive : Boolean = false) : Mapper<TestSession.Configuration, JSONObject> {
    override fun map(input: TestSession.Configuration): JSONObject {
        val root = JSONObject()

        root.put("session_type", input.sessionType.name)
        root.put("provision_mode", input.provisionMode.name)
        root.put("provision_mode_data", input.provisionModeData)
        root.put("classifier_mode", input.classifierMode.name)

        if (!stripSensitive) {
            input.flavorText?.let {
                root.put("flavor_text", it)
            }

            input.flavorTextTwo?.let {
                root.put("flavor_text_two", it)
            }
        }

        input.outputSessionTranslatorId?.let {
            root.put("output_session_translator_id", it)
        }
        input.outputResultTranslatorId?.let {
            root.put("output_result_translator_id", it)
        }

        if(!stripSensitive) {
            input.cloudworksDns?.let {
                root.put("cloudworks_dns", it)
            }
        }

        input.cloudworksContext?.let {
            root.put("cloudworks_context", it)
        }

        val flags = JSONObject(input.flags)
        root.put("flags", flags)

        return root
    }
}


class ResultToJson() : Mapper<TestSession.TestResult, JSONObject> {
    override fun map(input: TestSession.TestResult): JSONObject {
        val root = JSONObject()
        input.timeRead?.let {
            root.put("time_read", getIsoUTCTimestamp(it))
        }

        input.mainImage?.let {
            root.put("main_image_path", it)
        }

        if(input.images.size > 0) {
            val images = JSONObject()
            for (image in input.images) {
                images.put(image.key, image.value)
            }
            root.put("images",images)
        }

        root.put("results", JSONObject(input.results.toMap()))

        root.put("results_classifier", JSONObject(input.classifierResults.toMap()))

        return root
    }
}

class MetricsToJson() : Mapper<TestSession.Metrics, JSONObject> {
    override fun map(input: TestSession.Metrics): JSONObject {
        val root = JSONObject()

        val data = JSONObject()
        for (entry in input.data) {
            data.put(entry.key, entry.value)
        }
        root.put("data", data)

        return root
    }
}


class SessionToJson(val stripSensitive : Boolean = false) : Mapper<TestSession, JSONObject> {
    override fun map(input: TestSession): JSONObject {
        val root = JSONObject()

        root.put("id", input.sessionId)
        root.put("state", input.state.name)
        root.put("test_profile_id", input.testProfileId)
        root.put("time_started", getIsoUTCTimestamp(input.timeStarted))
        root.put("time_resolved", getIsoUTCTimestamp(input.timeResolved))
        input.timeExpired?.let {
            root.put("time_expired", getIsoUTCTimestamp(it))
        }

        root.put("configuration", ConfigurationToJson(stripSensitive).map(input.configuration))

        input.result?.let {
            root.put("result", ResultToJson().map(it))
        }

        root.put("metrics", MetricsToJson().map(input.metrics))

        return root
    }
}


class TraceToJson() : Mapper<TestSessionTraceEvent, JSONObject> {
    override fun map(input: TestSessionTraceEvent): JSONObject {
        val root = JSONObject()

        root.put("timestamp", input.timestamp)
        root.put("tag", input.eventTag)
        root.put("message", input.eventMessage)
        input.eventJson?.let {
            root.put("json", JSONObject(it))

        }
        input.sandboxObjectId?.let {
            root.put("media_key", it)

        }
        return root
    }
}