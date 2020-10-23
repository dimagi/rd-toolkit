package org.rdtoolkit

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.rdtoolkit.model.session.getDatabase
import org.rdtoolkit.service.NOTIFICATION_TAG_TEST_ID
import org.rdtoolkit.service.TestTimerService
import java.util.*

class RdtApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val startScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val context : Context = this
        startScope.launch {
            for (sessionId in getDatabase(context).testSessionDao().getPendingSessionIds(Date())) {
                restartServiceTimer(sessionId)
            }
        }
    }

    private fun restartServiceTimer(sessionId : String) {
        val testTimerIntent = Intent(this, TestTimerService::class.java)
        testTimerIntent.putExtra(NOTIFICATION_TAG_TEST_ID, sessionId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(testTimerIntent)
        } else {
            startService(testTimerIntent)
        }
    }
}
