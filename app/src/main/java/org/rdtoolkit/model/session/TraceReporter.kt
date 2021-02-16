package org.rdtoolkit.model.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.rdtoolkit.support.model.session.TestSessionTraceEvent
import org.rdtoolkit.util.getIsoUTCTimestamp
import java.util.*

class TraceReporter(val repo : SessionRepository, val scope : CoroutineScope) {
    private lateinit var sessionId : String

    fun trace(tag : String, message : String, eventJson : String? = null, sandboxObjectId : String? = null) {
        if (!this::sessionId.isInitialized) {
            //TODO : queue preinit traces?
            return;
        }

        //TODO: Ideally we'd have a single dispatcher for logging which can ensure that requests are
        //processed FIFO
        scope.launch(Dispatchers.IO) {
            eventJson?.apply {
                //if a json payload was provided, ensure that it is well formed
                JSONObject(eventJson)
            }

            repo.recordTraceEvent(TestSessionTraceEvent(sessionId, getIsoUTCTimestamp(Date())!!, tag, message, eventJson, sandboxObjectId))
        }
    }

    fun enableTraceRecording(sessionId : String) {
        this.sessionId = sessionId
    }
}