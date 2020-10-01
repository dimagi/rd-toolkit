package org.rdtoolkit.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.navigation.Navigation
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

var REQUEST_PHOTO_CAPTURE = 1

class DefaultImageCaptureComponent : TestImageCaptureComponent {

    var returnPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(context: Context): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "TEST_" + timeStamp + "_"
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)

        returnPhotoPath = image.absolutePath
        return image
    }

    override fun triggerCallout(activity: Activity) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            photoFile = try {
                createImageFile(activity)
            } catch (ex: IOException) {
                throw RuntimeException(ex)
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(activity,
                        "org.rdtoolkit.fileprovider",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                activity.startActivityForResult(takePictureIntent, REQUEST_PHOTO_CAPTURE)
            }
        }
    }
    override fun processIntentCallback(requestCode : Int,
                                       resultCode : Int,
                                       data : Intent?) : Boolean {
        if (requestCode == REQUEST_PHOTO_CAPTURE && resultCode == Activity.RESULT_OK) {
            val returnPhoto = File(returnPhotoPath)
            if (returnPhoto.exists()) {
                return true
            }
        }
        return false
    }

    override fun getResultImage() : String {
        return returnPhotoPath!!
    }
}