package com.example.motionmate.detectedactivity

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat

const val ACTIVITY_UPDATES_INTERVAL = 1000L

class DetectedActivityService : Service() {

    inner class LocalBinder : Binder() {

        val serverInstance: DetectedActivityService
            get() = this@DetectedActivityService
    }

    override fun onBind(p0: Intent?): IBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        requestActivityUpdates()
    }

    private fun requestActivityUpdates() {
    }

    private fun removeActivityUpdates() {
    }

    override fun onDestroy() {
        super.onDestroy()
        removeActivityUpdates()
        NotificationManagerCompat.from(this).cancel(DETECTED_ACTIVITY_NOTIFICATION_ID)
    }
}