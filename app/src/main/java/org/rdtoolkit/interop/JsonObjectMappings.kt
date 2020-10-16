package org.rdtoolkit.interop

import org.json.JSONObject
import org.rdtoolkit.support.model.Mapper
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.util.getIsoUTCTimestamp


class ConfigurationToJson : Mapper<TestSession.Configuration, JSONObject> {
    override fun map(input: TestSession.Configuration): JSONObject {
        val root = JSONObject()

        root.put("provision_mode", input.provisionMode.name)
        root.put("provision_mode_data", input.provisionModeData)
        root.put("classifier_mode", input.classifierMode.name)

        input.flavorText?.let {
            root.put("flavor_text", it)

        }

        input.flavorTextTwo?.let {
            root.put("flavor_text_two", it)

        }
        input.flavorText?.let {
            root.put("flavor_text", it)

        }

        root.put("session_type", input.sessionType.name)
        input.outputSessionTranslatorId?.let {
            root.put("output_session_translator_id", it)
        }
        input.outputResultTranslatorId?.let {
            root.put("output_result_translator_id", it)
        }

        val flags = JSONObject(input.flags)
        root.put("flags", flags)

        return root
    }
}


class ResultToJson : Mapper<TestSession.TestResult, JSONObject> {
    override fun map(input: TestSession.TestResult): JSONObject {
        val root = JSONObject()
        input.timeRead?.let {
            root.put("time_read", getIsoUTCTimestamp(it))
        }

        input.rawCapturedImageFilePath?.let {
            root.put("raw_image_file_path", it)
        }

        root.put("results", JSONObject(input.results.toMap()))

        root.put("results_classifier", JSONObject(input.classifierResults.toMap()))

        return root
    }
}


class SessionToJson : Mapper<TestSession, JSONObject> {
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

        root.put("configuration", ConfigurationToJson().map(input.configuration))

        input.result?.let {
            root.put("result", ResultToJson().map(it))
        }

        return root
    }
}

