package org.rdtoolkit.processing

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.rdtoolkit.interop.SessionToJson
import org.rdtoolkit.interop.TraceToJson
import org.rdtoolkit.support.model.ListMapperImpl
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.support.model.session.TestSessionTraceEvent
import java.io.File
import java.util.concurrent.TimeUnit


class CloudworksApi(dns: String, val sessionId : String, val context : Context) {
    /**
     * Connection read and write timeouts in seconds
     */
    val CONNECTION_READ_TIMEOUT = 120
    val CONNECTION_WRITE_TIMEOUT = 120

    private val dns = dns.removeSuffix("/")
    private val client = OkHttpClient.Builder()
            .readTimeout(CONNECTION_READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()

    fun submitSessionJson(session: TestSession) {
        val json = SessionToJson(true).map(session)
        val body = json.toString(4).toRequestBody(JSON)

        put(getSessionSubmissionEndpoint(), body)
    }

    fun submitTraceJson(traces : List<TestSessionTraceEvent>) {
        val envelope = JSONObject()
        envelope.put("entries", JSONArray(ListMapperImpl(TraceToJson()).map(traces)))
        val text = envelope.toString(4)

        val body = text.toRequestBody(JSON)

        put(getSessionTraceEndpoint(), body)
    }

    fun submitSessionMedia(key: String, file: File) {
        val body = file.asRequestBody(JPEG)
        val filename = file.name
        val url = getSessionMediaSubmissionEndpoint(key)
        val sessionBodyRequest = Request.Builder()
                .url(getSessionMediaSubmissionEndpoint(key))
                .put(body)
                .addHeader("Content-Disposition", "attachment; filename=${filename}")
                .build()

        val response = client.newCall(sessionBodyRequest).execute()

        if (!(response.code == 201 || response.code == 200 || response.code == 409)) {
            throw Exception("Invalid server response ${response.code} with body ${response.body?.string()}")
        }
    }

    fun put(url : String, body : RequestBody) {
        val sessionBodyRequest = Request.Builder()
                .url(url)
                .put(body)
                .build()

        val response = client.newCall(sessionBodyRequest).execute()

        if (!(response.code == 201 || response.code == 200 || response.code == 409)) {
            throw Exception("Invalid server response ${response.code} with body ${response.body?.string()}")
        }
    }

    private fun getSessionSubmissionEndpoint() : String {
        return "$dns/test_session/$sessionId/"
    }
    private fun getSessionTraceEndpoint() : String {
        return "$dns/test_session/$sessionId/logs/"
    }

    private fun getSessionMediaSubmissionEndpoint(key : String) : String {
        return "$dns/test_session/$sessionId/media/$key/"
    }

    companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
        val JPEG = "image/jpeg".toMediaType()
    }
}