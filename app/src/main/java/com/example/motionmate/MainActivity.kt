package com.example.motionmate

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.motionmate.detectedactivity.DetectedActivityService
import com.example.motionmate.transitions.TRANSITIONS_RECEIVER_ACTION
import com.example.motionmate.transitions.TransitionsReceiver
import com.example.motionmate.transitions.removeActivityTransitionUpdates
import com.example.motionmate.transitions.requestActivityTransitionUpdates
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*

const val KEY_IS_TRACKING_STARTED = "is_tracking_started"

class MainActivity : AppCompatActivity() {

    private var isTrackingStarted = false
        set(value) {
            resetBtn.visibility = if(value) View.VISIBLE else View.GONE
            field = value
        }

    private val transitionBroadcastReceiver: TransitionsReceiver = TransitionsReceiver().apply {
        action = { setDetectedActivity(it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isTrackingStarted = savedInstanceState.getBoolean(KEY_IS_TRACKING_STARTED, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_TRACKING_STARTED, isTrackingStarted)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(SUPPORTED_ACTIVITY_KEY)) {
            val supportedActivity = intent.getSerializableExtra(
                SUPPORTED_ACTIVITY_KEY) as SupportedActivity
            setDetectedActivity(supportedActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dateAndTimeTextView: TextView = findViewById(R.id.dateAndTime)
        val dateFormat = SimpleDateFormat("MMMM dd, hh:mm a", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        dateAndTimeTextView.text = currentTime

        startBtn.setOnClickListener {
            // Check if permission is granted
            if (isPermissionGranted()) {
                // Request the activity transition updates
                requestActivityTransitionUpdates()

                // Start background service which is tracking the activity and confidence
                startService(Intent(this, DetectedActivityService::class.java))
            } else {
                // Request permission if it is not granted
                requestPermission()
            }

            isTrackingStarted = true
            Toast.makeText(this, "You've started activity tracking", Toast.LENGTH_SHORT)
                .show()
        }
        stopBtn.setOnClickListener {
            // Stop tracking the activity transition updates
            removeActivityTransitionUpdates()

            // Stop service when a user clicks on the stop button
            stopService(Intent(this, DetectedActivityService::class.java))
            Toast.makeText(this, "You've stopped activity tracking", Toast.LENGTH_SHORT)
                .show()
        }
        resetBtn.setOnClickListener {
            resetTracking()
        }
    }

    private fun resetTracking() {
        isTrackingStarted = false
        setDetectedActivity(SupportedActivity.NOT_STARTED)
        removeActivityTransitionUpdates()
        stopService(Intent(this, DetectedActivityService::class.java))
    }

    override fun onResume() {
        super.onResume()
        // Register to the transition broadcast receiver
        registerReceiver(transitionBroadcastReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
    }

    override fun onPause() {
        // Unregister from the transition broadcast receiver
        unregisterReceiver(transitionBroadcastReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        removeActivityTransitionUpdates()
        // Stop the DetectedActivityService
        super.onDestroy()
    }

    private fun setDetectedActivity(supportedActivity: SupportedActivity) {
        // Consume supported activity
        println("Inside detected activity:" + getString(supportedActivity.activityText))
        imageView.setImageDrawable(ContextCompat.getDrawable(this, supportedActivity.activityImage))
        messageText.text = getString(supportedActivity.activityText)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // When request permission result comes, check if it is granted
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACTIVITY_RECOGNITION).not() &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_DENIED) {
            showSettingsDialog(this)
        } else if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION &&
            permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("permission_result", "permission granted")
            isTrackingStarted = true
        } else {
            startService(Intent(this, DetectedActivityService::class.java))
            requestActivityTransitionUpdates()
        }
        // Start the service and request transition updates when a user grants a permission
    }
}