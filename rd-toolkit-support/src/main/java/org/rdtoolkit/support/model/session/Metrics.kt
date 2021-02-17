package org.rdtoolkit.support.model.session

import android.content.Context
import android.os.Build

const val METRICS_NUMBER_OF_CAPTURE_ATTEMPTS = "image_capture_attempts"
const val METRICS_VALUE_TRUE = "true"
const val METRICS_INSTRUCTIONS_VIEWED = "instructions_viewed"
const val METRICS_JOB_AID_VIEWED = "job_aid_viewed"
const val METRICS_DEVICE_TYPE = "device_type"
const val METRICS_ANDROID_SDK = "android_sdk"
const val METRICS_TOOLKIT_VERSION = "toolkit_version"

fun TestSession.Metrics.setCaptureAttempts(numberOfAttempts: Int) {
    this.data[METRICS_NUMBER_OF_CAPTURE_ATTEMPTS] = numberOfAttempts.toString()
}

fun TestSession.Metrics.setInstructionsViewed() {
    this.data[METRICS_INSTRUCTIONS_VIEWED] = METRICS_VALUE_TRUE
}

fun TestSession.Metrics.setJobAidViewed() {
    this.data[METRICS_JOB_AID_VIEWED] = METRICS_VALUE_TRUE
}

fun TestSession.Metrics.setDeviceMetadata(context : Context) {
    this.data[METRICS_DEVICE_TYPE] = Build.MODEL
    this.data[METRICS_ANDROID_SDK] = Build.VERSION.SDK_INT.toString()
    var versionName : String = "unknown"
    try {
        versionName = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
    } catch (e : Exception) {

    }
    this.data[METRICS_TOOLKIT_VERSION] = versionName



}