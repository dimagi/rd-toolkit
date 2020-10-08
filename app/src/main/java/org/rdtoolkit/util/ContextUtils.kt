package org.rdtoolkit.util

import android.content.Context
import org.rdtoolkit.R

class ContextUtils(val context : Context) {
    fun getReadableTime(time: Int): String? {
        return if (time < 60) {
            String.format(context.getString(R.string.time_in_seconds), time)
        } else {
            String.format(context.getString(R.string.time_in_minutes), Math.floor(time / 60.toDouble()).toInt())
        }
    }
}