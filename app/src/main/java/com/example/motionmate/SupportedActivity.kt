package com.example.motionmate

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.DetectedActivity.IN_VEHICLE
import java.lang.IllegalArgumentException


const val SUPPORTED_ACTIVITY_KEY = "activity_key"
enum class SupportedActivity(
    @DrawableRes val activityImage: Int,
    @StringRes val activityText: Int
) {

    NOT_STARTED(R.drawable.uottawa_ver_black, R.string.welcome_message),
    STILL(R.drawable.idle, R.string.still_text),
    WALKING(R.drawable.walking, R.string.walking_text),
    RUNNING(R.drawable.running, R.string.running_text),
    IN_VEHICLE(R.drawable.vehicle, R.string.vehicle_text);

    companion object {
        fun fromActivityType(type: Int): SupportedActivity = when (type) {
            DetectedActivity.STILL -> STILL
            DetectedActivity.WALKING -> WALKING
            DetectedActivity.RUNNING -> RUNNING
            DetectedActivity.IN_VEHICLE -> IN_VEHICLE
            else -> throw IllegalArgumentException("activity $type not supported")
        }
    }
}