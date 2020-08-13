package org.rdtoolkit.util

import android.content.Context
import androidx.fragment.app.Fragment

class ContextUtils(val context : Context) {
    fun getReadableTime(time: Int): String? {
        return if (time < 60) {
            String.format("%d Seconds", time)
        } else {
            String.format("%d Minutes", Math.floor(time / 60.toDouble()).toInt())
        }
    }
}