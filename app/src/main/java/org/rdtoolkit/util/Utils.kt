package org.rdtoolkit.util

fun getFormattedTimeForSpan(span : Long) : String{
    val minutes: Int = (span / (60 * 1000)).toInt()
    val seconds: Int = (span / 1000 % 60).toInt()
    return String.format("%d:%02d", minutes, seconds)
}