package org.rdtoolkit.support.model.session

const val FLAG_PREFIX_KEY = "FLAG_"

const val FLAG_SESSION_NO_EARLY_READS = "FLAG_NO_EARLY_READS"
const val FLAG_SESSION_NO_EXPIRATION_OVERRIDE = "FLAG_NO_OVERRIDE"
const val FLAG_SESSION_TESTING_QA = "FLAG_TESTING_QA"

const val FLAG_CALLING_PACKAGE = "FLAG_ANDROID_CALLING_PACKAGE"

const val FLAG_VALUE_SET = "TRUE"
const val FLAG_VALUE_UNSET = "FALSE"

const val FLAG_SECONDARY_CAPTURE = "FLAG_CAPTURE_SECONDARY_INPUT"
const val FLAG_SECONDARY_PARAMS = "FLAG_CAPTURE_SECONDARY_PARAMS"

const val FLAG_CAPTURE_PARAMS = "FLAG_CAPTURE_PARAMS"
const val FLAG_CAPTURE_REQUIREMENTS = "FLAG_CAPTURE_REQUIREMENTS"

fun TestSession.Configuration.wasSecondaryCaptureRequested() : Boolean {
    return this.flags[FLAG_SECONDARY_CAPTURE] == FLAG_VALUE_SET
}

fun TestSession.Configuration.getSecondaryCaptureParams() : Map<String, String> {
    val map = HashMap<String, String>()
    this.flags[FLAG_SECONDARY_PARAMS]?.let { map[FLAG_CAPTURE_PARAMS] = it}
    return map
}