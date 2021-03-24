package org.rdtoolkit.support.model.session

const val FLAG_PREFIX_KEY = "FLAG_"

const val FLAG_SESSION_NO_EARLY_READS = "FLAG_NO_EARLY_READS"
const val FLAG_SESSION_NO_EXPIRATION_OVERRIDE = "FLAG_NO_OVERRIDE"
const val FLAG_SESSION_TESTING_QA = "FLAG_TESTING_QA"

const val FLAG_CALLING_PACKAGE = "FLAG_ANDROID_CALLING_PACKAGE"

const val FLAG_VALUE_SET = "TRUE"
const val FLAG_VALUE_UNSET = "FALSE"

const val FLAG_SESSION_CLOUDWORKS_CAPTURE_TRACE= "FLAG_CLOUDWORKS_CAPTURE_TRACE"
const val FLAG_SESSION_CLOUDWORKS_FULL_IMAGE_SUBMISSION= "FLAG_SESSION_CLOUDWORKS_FULL_IMAGE_SUBMISSION"

const val FLAG_CAPTURE_ALLOW_INDETERMINATE = "FLAG_CAPTURE_ALLOW_INDETERMINATE"

const val FLAG_SECONDARY_CAPTURE = "FLAG_CAPTURE_SECONDARY_INPUT"
const val FLAG_SECONDARY_PARAMS = "FLAG_CAPTURE_SECONDARY_PARAMS"

const val FLAG_CAPTURE_PARAMS = "FLAG_CAPTURE_PARAMS"
const val FLAG_CAPTURE_REQUIREMENTS = "FLAG_CAPTURE_REQUIREMENTS"

fun TestSession.Configuration.isCloudworksActive() : Boolean {
    return this.cloudworksDns != null
}

fun TestSession.Configuration.isTraceEnabled() : Boolean {
    return this.isCloudworksActive() && isFlagSet(FLAG_SESSION_CLOUDWORKS_CAPTURE_TRACE, true)
}

fun TestSession.Configuration.isComprehensiveImageSubmissionEnabled() : Boolean {
    return this.isCloudworksActive() && isFlagSet(FLAG_SESSION_CLOUDWORKS_FULL_IMAGE_SUBMISSION)
}

fun TestSession.Configuration.isFlagSet(flag : String, default : Boolean = false) : Boolean {
    return if (default)  {
        FLAG_VALUE_UNSET != this.flags[flag]
    } else {
        FLAG_VALUE_SET == this.flags[flag]
    }
}

fun TestSession.Configuration.wasSecondaryCaptureRequested() : Boolean {
    return isFlagSet(FLAG_SECONDARY_CAPTURE)
}

fun TestSession.Configuration.getSecondaryCaptureParams() : Map<String, String> {
    val map = HashMap<String, String>()
    this.flags[FLAG_SECONDARY_PARAMS]?.let { map[FLAG_CAPTURE_PARAMS] = it}
    return map
}


fun TestSession.Configuration.setCaptureFlag(flag: String) {
    this.flags = flags.toMutableMap().apply { this[FLAG_CAPTURE_PARAMS] =
            (this[FLAG_CAPTURE_PARAMS]?.split(" ") ?: listOf())
            .toMutableSet().apply { add(flag) }.joinToString(" ") }
}

fun TestSession.Configuration.removeCaptureFlag(flag: String) {
    this.flags = flags.toMutableMap().apply { this[FLAG_CAPTURE_PARAMS] =
            (this[FLAG_CAPTURE_PARAMS]?.split(" ") ?: listOf())
                    .toMutableSet().apply { remove(flag) }.joinToString(" ")}
}

fun TestSession.Configuration.checkCaptureFlag(flag: String) : Boolean {
    return (this.flags[FLAG_CAPTURE_PARAMS]?.split(" ") ?: listOf()).contains(flag)
}
