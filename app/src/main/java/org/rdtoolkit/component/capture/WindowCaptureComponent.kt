package org.rdtoolkit.component.capture

import android.Manifest
import android.app.Activity
import android.content.Intent
import org.rdtoolkit.component.TestImageCaptureComponent
import java.io.File

class WindowCaptureComponent : TestImageCaptureComponent {

    var returnPhotoPath : String? = null

    override fun triggerCallout(activity: Activity) {
        val calloutIntent = Intent(activity, WindowCaptureActivity::class.java)

        activity.startActivityForResult(calloutIntent, RESULT_WINDOWED_CAPTURE)
    }

    override fun processIntentCallback(requestCode : Int,
                                       resultCode : Int,
                                       data : Intent?) : Boolean {
        if (requestCode == RESULT_WINDOWED_CAPTURE && resultCode == Activity.RESULT_OK) {

            val returnPhoto = File(data!!.getStringExtra(WindowCaptureActivity.EXTRA_CROPPED_IMAGE))
            if (returnPhoto.exists()) {
                returnPhotoPath = returnPhoto.absolutePath
                return true
            }
        }
        return false
    }


    override fun getResultImage(): String {
        return returnPhotoPath!!
    }

    companion object {
        const val RESULT_WINDOWED_CAPTURE = 2
    }

}