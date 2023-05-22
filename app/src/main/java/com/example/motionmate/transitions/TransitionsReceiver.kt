package com.example.motionmate.transitions

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build.VERSION_CODES.S
import android.widget.Toast
import com.example.motionmate.BuildConfig
import com.example.motionmate.DatabaseHandler
import com.example.motionmate.R
import com.example.motionmate.SupportedActivity
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

const val TRANSITIONS_RECEIVER_ACTION = "${BuildConfig.APPLICATION_ID}_transitions_receiver_action"
private const val TRANSITION_PENDING_INTENT_REQUEST_CODE = 200

class TransitionsReceiver: BroadcastReceiver() {

    var action: ((SupportedActivity) -> Unit)? = null
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var context: Context
    var startTime: Long = System.currentTimeMillis() / (1000)
    var newStartTime: Long = 0
    var endTime: Long = 0
    var duration: Long = 0

    companion object {

        fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
            return PendingIntent.getBroadcast(context, TRANSITION_PENDING_INTENT_REQUEST_CODE, intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Extract result from the intent and handle transition events
        // 1
        this.context = context ?: return
        println(ActivityTransitionResult.hasResult(intent))
        if (ActivityTransitionResult.hasResult(intent)) {
            // 2
            val result = ActivityTransitionResult.extractResult(intent)
            println(result.toString())
            result?.let { handleTransitionEvents(it.transitionEvents) }
        }

    }
    private fun toTransitionType(transitionType: Int): String? {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
    }
    private fun toActivityString(activity: Int): String? {
        return when (activity) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            else -> "UNKNOWN"
        }
    }

    private fun playBackgroundMusic() {
        // condition added to avoid the background music from playing again if one instance of music is already playing
        mediaPlayer = MediaPlayer.create(context, R.raw.sample_audio)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    fun stopBackgroundMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    // Filter Enter transitions, map them to SupportedAction, and invoke action with them
    private fun handleTransitionEvents(transitionEvents: List<ActivityTransitionEvent>) {
        transitionEvents
            // 3
            .filter { it.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER }
            // 4
            .forEach { action?.invoke(SupportedActivity.fromActivityType(it.activityType)) }

        for (transitionEvent in transitionEvents) {
            val day = SimpleDateFormat("dd/MM/yy", Locale.US).format(Date())
            val activity = toActivityString(transitionEvent.getActivityType())

            val info = "Transition: " + toActivityString(transitionEvent.getActivityType()) +
                    " (" + toTransitionType(transitionEvent.getTransitionType()) + ")" + "   " +
                    SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

            println(info)

            if(toTransitionType(transitionEvent.getTransitionType()) == "ENTER") {
                newStartTime = System.currentTimeMillis() / (1000)
            }
            if(toTransitionType(transitionEvent.getTransitionType()) == "EXIT") {
                endTime = System.currentTimeMillis() / (1000)
                duration = (endTime - startTime) - (endTime - startTime) %1

                var time_in_string = String.format("%d minute and %d secs",
                    TimeUnit.SECONDS.toMinutes(duration),
                    duration - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(duration))
                )
                var startTime_inString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(startTime*1000)
                var endTime_inString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(endTime*1000)

                Toast.makeText(context, "Your $activity activity duration was $time_in_string", Toast.LENGTH_SHORT).show()
                println("startTime: "+ startTime_inString + " endTime: "+endTime_inString+ " Duration in mins: "+ TimeUnit.SECONDS.toMinutes(duration)+" day: " + day + " Activity: "+ activity)
                 // day, time, duration and activity to be stored in the DB
                val db = DatabaseHandler(context)
                if (activity != null) {
                    db.insertData(day, startTime_inString, TimeUnit.SECONDS.toMinutes(duration).toString(), activity)
                }
                startTime = newStartTime
            }

            if (toActivityString(transitionEvent.getActivityType()) == "RUNNING") {
                if(toTransitionType(transitionEvent.getTransitionType()) == "ENTER") {
                    playBackgroundMusic()
                }
                if(toTransitionType(transitionEvent.getTransitionType()) == "EXIT") {
                    stopBackgroundMusic()
                }
            }
        }
    }
}