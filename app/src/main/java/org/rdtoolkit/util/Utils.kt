package org.rdtoolkit.util

import java.text.SimpleDateFormat
import java.util.*

fun getFormattedTimeForSpan(span : Long) : String{
    val minutes: Int = (span / (60 * 1000)).toInt()
    val seconds: Int = (span / 1000 % 60).toInt()
    return String.format("%d:%02d", minutes, seconds)
}

fun getIsoUTCTimestamp(input : Date) : String? {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    return formatter.format(input)
}