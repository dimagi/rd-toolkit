package org.rdtoolkit.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rdtoolkit.MainActivity
import org.rdtoolkit.R
import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.util.InjectorUtils

const val CHANNEL_ID_COUNTDOWN ="Test"
const val CHANNEL_ID_FIRE ="Fire"

const val SERVICE_TIMER = 1
const val NOTIFICATION_TAG_TEST_ID = "test_id"

class TestTimerService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job

        createNotificationChannels();

        var testId : String = intent?.getStringExtra(NOTIFICATION_TAG_TEST_ID)!!;

        var builder = getNotificationBuilder();

        NotificationManagerCompat.from(this)
                .notify(testId, SERVICE_TIMER, builder.setContentText("Preparing Timer...").build())

        lifecycleScope.launch(Dispatchers.IO) {
            var session = InjectorUtils.provideSessionRepository(this@TestTimerService).load(testId)

            launch(Dispatchers.Main) {
                startResolvingTestTimer(session)
            }

        }
        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    private fun startResolvingTestTimer(session: TestSession) {
        var builder = getNotificationBuilder();
        var timer = object : CountDownTimer(session.timeResolved.time - System.currentTimeMillis(), 500) {
            override fun onTick(millisUntilFinished: Long) {
                builder.setContentTitle("Test " + session.flavorText)
                builder.setContentText("Time Remaining: " + getFormattedTimeForSpan(millisUntilFinished))
                NotificationManagerCompat.from(this@TestTimerService)
                        .notify(session.sessionId, SERVICE_TIMER, builder.build())
            }

            override fun onFinish() {
                var manager = NotificationManagerCompat.from(this@TestTimerService)
                manager.cancel(session.sessionId, SERVICE_TIMER);
                beginTestReady(session)
            }
        }.start();
    }

    private fun beginTestReady(session: TestSession) {
        var manager = NotificationManagerCompat.from(this)

        var builder = getFinishedNotificationBuilder()
        manager.notify(session.sessionId, SERVICE_TIMER,
                builder.setContentText("Test Ready").build())

        if (session.timeExpired == null) {
            //No expirey date set for this test
            return;
        }

        var timer = object : CountDownTimer(session.timeExpired.time - System.currentTimeMillis(), 500) {
            override fun onTick(millisUntilFinished: Long) {
                builder.setContentText("Results valid for: " + getFormattedTimeForSpan(millisUntilFinished))
                NotificationManagerCompat.from(this@TestTimerService)
                        .notify(session.sessionId, SERVICE_TIMER, builder.build())
            }

            override fun onFinish() {
                builder.setContentTitle("Test Expired" + session.flavorText)
                builder.setContentText("Test is no longer valid to read")
                NotificationManagerCompat.from(this@TestTimerService)
                        .notify(session.sessionId, SERVICE_TIMER, builder.build())

            }
        }.start();

    }

    private fun getFormattedTimeForSpan(span : Long) : String{
        val minutes: Int = (span / (60 * 1000)).toInt()
        val seconds: Int = (span / 1000 % 60).toInt()
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val countdown = NotificationChannel(CHANNEL_ID_COUNTDOWN,
                    "Running Timers",
                    NotificationManager.IMPORTANCE_LOW);
            countdown.description = "Timers that are processing"

            val fireChannel = NotificationChannel(CHANNEL_ID_FIRE,
                    "Triggered Timers",
                    NotificationManager.IMPORTANCE_HIGH)
            fireChannel.description = "Notifications for timers which have fired"

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(countdown)
            notificationManager.createNotificationChannel(fireChannel)
        }

    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        // The PendingIntent to launch our activity if the user selects
        // this notification
        val title = "Timer Running..."
        val contentIntent = PendingIntent.getActivity(this,
                0, Intent(this, MainActivity::class.java), 0)
        return NotificationCompat.Builder(this, CHANNEL_ID_COUNTDOWN)
                .setOngoing(true)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_baseline_timer_24)
                .setContentIntent(contentIntent)
    }

    private fun getFinishedNotificationBuilder(): NotificationCompat.Builder {
        // The PendingIntent to launch our activity if the user selects
        // this notification
        val title = "Test Ready"
        val contentIntent = PendingIntent.getActivity(this,
                0, Intent(this, MainActivity::class.java), 0)
        return NotificationCompat.Builder(this, CHANNEL_ID_FIRE)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_baseline_timer_24)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}