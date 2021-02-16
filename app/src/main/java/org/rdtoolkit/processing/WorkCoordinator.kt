package org.rdtoolkit.processing

import android.content.Context
import androidx.work.*
import androidx.work.WorkRequest.MIN_BACKOFF_MILLIS
import org.rdtoolkit.model.session.AppRepository
import org.rdtoolkit.processing.ImageSubmissionWorker.Companion.DATA_FILE_PATH
import org.rdtoolkit.processing.ImageSubmissionWorker.Companion.DATA_MEDIA_KEY
import org.rdtoolkit.processing.ImageSubmissionWorker.Companion.TAG_MEDIA
import org.rdtoolkit.processing.SessionPurgeWorker.Companion.TAG_PURGE
import org.rdtoolkit.processing.SessionSubmissionWorker.Companion.TAG_SESSION
import org.rdtoolkit.processing.TraceSubmissionWorker.Companion.TAG_TRACES
import org.rdtoolkit.support.interop.INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_ID
import org.rdtoolkit.support.model.session.TestSession
import java.util.*
import java.util.concurrent.TimeUnit


class WorkCoordinator(val context : Context) {
    val manager = WorkManager.getInstance(context)
    fun processTestSession(session : TestSession, purgeImmediately : Boolean = false) {

        if (session.configuration.cloudworksDns != null) {
            manager.beginUniqueWork(getUniqueWorkRootTag(session.sessionId),
                        ExistingWorkPolicy.REPLACE,
                        getSessionSubmitRequest(session))
                    .then(getTraceSubmitter(session))
                    .then(getImageSubmitters(session))
                    .then(getPurgeRequest(session, purgeImmediately))
                    .enqueue()
        } else {
            manager.beginUniqueWork(getUniqueWorkRootTag(session.sessionId),
                        ExistingWorkPolicy.REPLACE,
                        getPurgeRequest(session, purgeImmediately))
                    .enqueue()
        }
    }

    private fun getSessionSubmitRequest(session: TestSession) : OneTimeWorkRequest {
        var sessionData = Data.Builder()
                .putString(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
                .putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS, session.configuration.cloudworksDns)
                .build()

        return OneTimeWorkRequest.Builder(SessionSubmissionWorker::class.java)
                .addTag(session.sessionId)
                .addTag(TAG_SESSION)
                .setInputData(sessionData)
                .setConstraints(getNetworkConstraints())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)
                .build()
    }

    private fun getTraceSubmitter(session: TestSession) : OneTimeWorkRequest {
        var sessionData = Data.Builder()
                .putString(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
                .putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS, session.configuration.cloudworksDns)
                .build()
        return OneTimeWorkRequest.Builder(TraceSubmissionWorker::class.java)
                .addTag(session.sessionId)
                .addTag(TAG_TRACES)
                .setConstraints(getNetworkConstraints())
                .setInputData(sessionData)
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)
                .build()

    }

    private fun getPurgeRequest(session: TestSession, purgeImmediately: Boolean) : OneTimeWorkRequest {
        var purgeData = Data.Builder()
                .putString(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
                .build()

        return OneTimeWorkRequest.Builder(SessionPurgeWorker::class.java)
                .addTag(session.sessionId)
                .addTag(TAG_PURGE)
                .setInputData(purgeData)
                .also {
                    if (!purgeImmediately) {
                        it.setInitialDelay(1, TimeUnit.DAYS)
                    }
                }
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS).build()
    }

    private fun getImageSubmitters(session: TestSession) : List<OneTimeWorkRequest> {
        val list = ArrayList<OneTimeWorkRequest>()
        session.result?.let {
            for (image in it.images) {
                list.add(getImageSubmitter(image.key, image.value, session))
            }
        }
        return list
    }

    private fun getImageSubmitter(key: String, file : String, session: TestSession) : OneTimeWorkRequest {
        var imageData = Data.Builder()
                .putString(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
                .putString(INTENT_EXTRA_RDT_CONFIG_CLOUDWORKS_DNS, session.configuration.cloudworksDns)
                .putString(DATA_MEDIA_KEY, key)
                .putString(DATA_FILE_PATH, file)
                .build()

        return OneTimeWorkRequest.Builder(ImageSubmissionWorker::class.java)
                .addTag(session.sessionId)
                .addTag(TAG_MEDIA)
                .setInputData(imageData)
                .setConstraints(getNetworkConstraints())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)
                .build()
    }

    private fun getNetworkConstraints() : Constraints {
        return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(AppRepository(context).isNetworkRestrictedByBattery())
                .build()

    }

    companion object {

        @JvmStatic
        fun getUniqueWorkRootTag(sessionId : String) : String{
            return "session_work_${sessionId}"
        }
    }
}