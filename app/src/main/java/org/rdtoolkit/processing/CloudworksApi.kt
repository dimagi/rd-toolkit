package org.rdtoolkit.processing

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
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
import java.io.IOException
import java.util.concurrent.TimeUnit


class CloudworksApi(dns: String, val sessionId : String, val context : Context) {
    private val dns = dns.removeSuffix("/")

    fun submitSessionJson(session: TestSession) {
        val json = SessionToJson(true).map(session)
        val body = json.toString(4).toRequestBody(JSON)

        put(getSessionSubmissionEndpoint(), body, true)
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

        val sessionBodyRequest = Request.Builder()
                .url(getSessionMediaSubmissionEndpoint(key))
                .put(body)
                .addHeader("Content-Disposition", "attachment; filename=${filename}")
                .build()

        val response = execute(HttpClient.client.newCall(sessionBodyRequest))

        response.use { response ->
            if (!(response.code == 201 || response.code == 200 || response.code == 409)) {
                throw Exception("Invalid server response ${response.code} with body ${response.body?.string()}")
            }
        }
    }

    fun put(url : String, body : RequestBody, dontQueue : Boolean = false) {
        val sessionBodyRequest = Request.Builder()
                .url(url)
                .put(body)
                .build()

        val call = HttpClient.client.newCall(sessionBodyRequest)
        val response = if (dontQueue) call.execute() else execute(call)

        response.use { response ->
            if (!(response.code == 201 || response.code == 200 || response.code == 409)) {
                throw Exception("Invalid server response ${response.code} with body ${response.body?.string()}")
            }
        }
    }

    /**
     * Execute HTTP call and synchronously wait for a result.
     *
     * Used instead of the execute() method to respect limitations from the dispatcher
     *
     */
    private fun execute(call : Call): Response {
        var callExcept : Exception? = null
        var callResponse : Response ? = null
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callExcept = e
            }

            override fun onResponse(call: Call, response: Response) {
                callResponse = response;
            }

        })
        try {
            while (true) {
                callResponse?.let { return it }
                callExcept?.let { throw it }

                //Since this is called from a worker, there's an implicit timeout for the worker's
                //runtime. If the worker gets stale the thread will get interrupted.
                Thread.sleep(100)
            }
        } catch(e : Exception) {
            call.cancel()
            throw e
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

object HttpClient {
    /**
     * Connection read and write timeouts in seconds
     */
    val CONNECTION_READ_TIMEOUT = 120
    val CONNECTION_WRITE_TIMEOUT = 120

    val client =  OkHttpClient.Builder()
            .readTimeout(CONNECTION_READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(CONNECTION_WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .dispatcher(Dispatcher().also { it.maxRequestsPerHost = 2 })
            .build()
}
