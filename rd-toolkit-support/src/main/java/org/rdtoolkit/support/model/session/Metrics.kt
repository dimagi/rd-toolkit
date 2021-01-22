package org.rdtoolkit.support.model.session

const val METRICS_NUMBER_OF_CAPTURE_ATTEMPTS = "image_capture_attempts"
const val METRICS_VALUE_TRUE = "true"
const val METRICS_INSTRUCTIONS_VIEWED = "instructions_viewed"
const val METRICS_JOB_AID_VIEWED = "job_aid_viewed"

fun TestSession.Metrics.setCaptureAttempts(numberOfAttempts: Int) {
    this.data[METRICS_NUMBER_OF_CAPTURE_ATTEMPTS] = numberOfAttempts.toString()
}

fun TestSession.Metrics.setInstructionsViewed() {
    this.data[METRICS_INSTRUCTIONS_VIEWED] = METRICS_VALUE_TRUE
}

fun TestSession.Metrics.setJobAidViewed() {
    this.data[METRICS_JOB_AID_VIEWED] = METRICS_VALUE_TRUE
}

