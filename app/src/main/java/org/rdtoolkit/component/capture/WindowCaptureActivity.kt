package org.rdtoolkit.component.capture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_window_capture.*
import org.rdtoolkit.R
import org.rdtoolkit.support.interop.RdtIntentBuilder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val EXTRA_RETICLE_RATIO = "windowed_capture_reticle_ratio"

class WindowCaptureActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null

    @SuppressLint("UnsafeExperimentalUsageError")
    private val rotatingCameraFilter = CurrentCameraFilter()

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_window_capture)

        setTargetReticleRatio()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto() }

        camera_rotate_button.setOnClickListener { rotateCameras() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        capture_window_camera_preview.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int,
                                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                updateReticleMapping()
            }
        })

        capture_window_test_reticle.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int,
                                        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                updateReticleMapping()
            }
        })

    }

    private fun setTargetReticleRatio() {
        this.intent.getStringExtra(EXTRA_RETICLE_RATIO)?.let {
            if (Regex("^[0-9]+:[0-9]+$").matches(it)) {
                (capture_window_test_reticle.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = it
            } else {
                throw Exception("Invalid requested reticle ratio $it")
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun rotateCameras() {
        rotatingCameraFilter.rotate()
        startCamera()
    }

    var reticalProportions = Pair(Rational(0,0), Rational(0,0))

    fun updateReticleMapping() {
        reticalProportions = Pair(
            Rational(capture_window_test_reticle.width, capture_window_camera_preview.width),
            Rational( capture_window_test_reticle.height, capture_window_camera_preview.height)
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                        getString(R.string.error_message_grant_camera),
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                            it.setSurfaceProvider(capture_window_camera_preview.surfaceProvider)
                    }


            imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(LENS_FACING_BACK)
                    .addCameraFilter(rotatingCameraFilter)
                    .build()

            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            var camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            if (rotatingCameraFilter.areMultipleCamerasAvailable()) {
                camera_rotate_button.visibility = View.VISIBLE
            } else {
                camera_rotate_button.visibility = View.GONE
            }

            val aspectRatio = getAspectRatio(camera)
            (capture_window_viewpane.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "${aspectRatio.numerator}:${aspectRatio.denominator}"


        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun getAspectRatio(camera: Camera): Rational {
        val cameraId = Camera2CameraInfo.fromCameraInfo(camera.cameraInfo).cameraId
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val outputSizes = streamConfigurationMap!!.getOutputSizes(ImageFormat.JPEG);

        var largestDensity = 0
        var largestSize = Rational(0,0)
        for (size in outputSizes) {
            with(size.width * size.height) {
                if (this > largestDensity) {
                    largestDensity = this
                    largestSize = Rational(size.width, size.height)
                }
            }
        }
        return largestSize
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                throw exc
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val croppedFile = cropFileForReticle(photoFile, reticalProportions)
                val returnData = Intent()
                returnData.putExtra(EXTRA_CROPPED_IMAGE, croppedFile.first.absolutePath)
                returnData.putExtra(EXTRA_ORIGINAL_IMAGE, photoFile.absolutePath)
                returnData.putExtra(EXTRA_RETICLE_RECT, croppedFile.second)
                setResult(Activity.RESULT_OK, returnData)
                finish()
            }
        })
    }

    private fun cropFileForReticle(file : File, reticleConstraints : Pair<Rational, Rational>) : Pair<File, Rect> {
        val sizeDecoderOptions = BitmapFactory.Options()
        sizeDecoderOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, sizeDecoderOptions)

        val size = Rect(0,0, sizeDecoderOptions.outWidth, sizeDecoderOptions.outHeight)
        if (size.width() < size.height()) {
            Toast.makeText(baseContext, "need to account for image ratio", Toast.LENGTH_SHORT).show()
            throw Exception("need to account for image ratio ")
        }

        val reticleCrop = Rect(0,0,
        (reticleConstraints.first.numerator * size.width()) / reticleConstraints.first.denominator,
        (reticleConstraints.second.numerator * size.height()) / reticleConstraints.second.denominator)

        reticleCrop.offsetTo(
                (size.width() - reticleCrop.width()) / 2,
                (size.height() - reticleCrop.height()) / 2
        )

        Log.i(LOG_TAG, "Original: ${size.width()}x${size.height()}")
        Log.i(LOG_TAG, "Cropped: ${reticleCrop.width()}x${reticleCrop.height()}")

        val croppedImage = BitmapRegionDecoder.newInstance(file.absolutePath, false).decodeRegion(reticleCrop, null)

        return Pair(saveCroppedFileVersion(file, croppedImage!!), reticleCrop)
    }

    private fun saveCroppedFileVersion(file: File, croppedImage: Bitmap): File {
        val directory = file.parentFile
        val destination = File(directory, file.nameWithoutExtension + "_cropped.jpg")
        destination.createNewFile();
        FileOutputStream(destination).use {
            croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, it)
            it.flush()
        }
        return destination
    }

    private fun getOutputDirectory() : File {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        private const val LOG_TAG = "WindowCaptureActivity"
        const val EXTRA_ORIGINAL_IMAGE = "ORIGNAL_PATH"
        const val EXTRA_CROPPED_IMAGE = "CROPPED_PATH"
        const val EXTRA_RETICLE_RECT = "RETICLE_RECT"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

@SuppressLint("UnsafeExperimentalUsageError")
class CurrentCameraFilter : CameraFilter {

    var currentCameraSet : LinkedHashSet<Camera>? = null

    override fun filter(cameras: LinkedHashSet<Camera>): LinkedHashSet<Camera> {
        if (currentCameraSet == null) {
            currentCameraSet = cameras
            return cameras
        } else {
            return currentCameraSet!!
        }
    }

    fun rotate() {
        if (currentCameraSet != null) {
            var resultingList = currentCameraSet!!.toList()
            Collections.rotate(resultingList,1)
            currentCameraSet = LinkedHashSet(resultingList)
        }
    }

    fun areMultipleCamerasAvailable() : Boolean {
        if (currentCameraSet != null) {
            return currentCameraSet!!.size > 1
        } else {
            return false
        }
    }
}