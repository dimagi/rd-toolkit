package org.rdtoolkit.component

import android.app.Activity
import android.content.Intent

interface TestImageCaptureComponent {
    fun triggerCallout(activity: Activity)

    fun processIntentCallback(requestCode : Int, resultCode : Int, data : Intent?) : Boolean {
        //Not required for all Components
        return false
    }

    fun getResultImage() : String
}