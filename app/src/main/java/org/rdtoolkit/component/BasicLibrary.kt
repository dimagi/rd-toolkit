package org.rdtoolkit.component

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

val COMPONENT_NATIVE_CAMERA_CAPTURE = "component_capture_native_camera"

class PlainCameraComponentManifest : ToolkitComponentManifest<TestImageCaptureComponent, NoConfig> {
    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        return setOf(COMPONENT_NATIVE_CAMERA_CAPTURE, TAG_READINESS_PRODUCTION)
    }

    override fun getComponent(config: NoConfig) : TestImageCaptureComponent {
        return DefaultImageCaptureComponent()
    }
}

class DefaultImageCaptureComponent : TestImageCaptureComponent(), ActivityLifecycleComponent {
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

    fun triggerCallout(activity: Activity) {
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
                activity.startActivityForResult(takePictureIntent, componentInterfaceId!!)
            }
        }
    }

    override fun processIntentCallback(requestCode: Int,
                                       resultCode: Int,
                                       data: Intent?) {
        if (requestCode == componentInterfaceId && resultCode == Activity.RESULT_OK) {
            val returnPhoto = File(returnPhotoPath)
            if (returnPhoto.exists()) {
                listener!!.testImageCaptured(returnPhotoPath!!)
            }
        }
    }

    override fun getResultImage(): String {
        return returnPhotoPath!!
    }

    override fun captureImage() {
        if (hasAllPermissions()) {
            triggerCallout(activity!!)
        } else {
            throw Exception("Didn't grant camera permissions when asked")
        }
    }
    override fun getRequiredPermissions() : Array<String>{
        return arrayOf(Manifest.permission.CAMERA)
    }
}