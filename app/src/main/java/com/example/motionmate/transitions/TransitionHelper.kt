package com.example.motionmate.transitions

import android.app.Activity
import android.util.Log
import com.example.motionmate.R
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

fun Activity.requestActivityTransitionUpdates() {
    // Request activity transition updates. Log success and failure
    val request = ActivityTransitionRequest(getActivitiesToTrack())
    val task = ActivityRecognitionClient(this).requestActivityTransitionUpdates(request,
        TransitionsReceiver.getPendingIntent(this))

    task.run {
        addOnSuccessListener {
            Log.d("TransitionUpdate", getString(R.string.transition_update_request_success))
        }
        addOnFailureListener {
            Log.d("TransitionUpdate", getString(R.string.transition_update_request_failed))
        }
    }
}

fun Activity.removeActivityTransitionUpdates() {
    // Remove activity transition updates. Log success and failure
    val task = ActivityRecognitionClient(this).removeActivityTransitionUpdates(
        TransitionsReceiver.getPendingIntent(this))

    task.run {
        addOnSuccessListener {
            Log.d("TransitionUpdate", getString(R.string.transition_update_remove_success))
        }
        addOnFailureListener {
            Log.d("TransitionUpdate", getString(R.string.transition_update_remove_failed))
        }
    }
}

// Return a list of Activity Transition that you want to track
private fun getActivitiesToTrack(): List<ActivityTransition> =
    // 1
    mutableListOf<ActivityTransition>()
        .apply {
            // 2
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())
            add(ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build())
        }