package org.rdtoolkit.component.capture

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.view.KeyEvent
import android.view.MotionEvent
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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WindowCaptureActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private var cameraControl: CameraControl? = null
    private var imagePreview: Preview? = null
    private var freezeFrame : Boolean = true

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

        camera_torch_button.setOnClickListener { toggleTorch() }

        if(intent.hasExtra(EXTRA_FILE_ROOT)) {
            outputDirectory = File(intent.getStringExtra(EXTRA_FILE_ROOT)!!)
        } else {
            outputDirectory = getOutputDirectory()
        }

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
    var isTorchEnabled = false

    private fun toggleTorch() {
        cameraControl?.let {
            isTorchEnabled = !isTorchEnabled
            it.enableTorch(isTorchEnabled)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            takePhoto()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setTargetReticleRatio() {
        if (this.intent.getStringExtra(EXTRA_CARD_RATIO) != null) {
            val cardRatio = this.intent.getStringExtra(EXTRA_CARD_RATIO)!!

            if (Regex("^[0-9]+:[0-9]+$").matches(cardRatio)) {
                (capture_window_card_reticle.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = cardRatio
                capture_window_card_reticle.visibility = View.VISIBLE
                capture_window_test_reticle.visibility = View.GONE
            } else {
                throw Exception("Invalid requested reticle ratio $cardRatio")
            }

        } else {
            this.intent.getStringExtra(EXTRA_RETICLE_RATIO)?.let {
                if (Regex("^[0-9]+:[0-9]+$").matches(it)) {
                    (capture_window_test_reticle.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = it
                    capture_window_card_reticle.visibility = View.GONE
                    capture_window_test_reticle.visibility = View.VISIBLE
                } else {
                    throw Exception("Invalid requested reticle ratio $it")
                }
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
        val view = if (this.intent.getStringExtra(EXTRA_CARD_RATIO) != null)  capture_window_card_reticle else capture_window_test_reticle

        reticalProportions = Pair(
            Rational(view.width, capture_window_camera_preview.width),
            Rational( view.height, capture_window_camera_preview.height)
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
            imagePreview = Preview.Builder()
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
                    this, cameraSelector, imagePreview, imageCapture)

            if (rotatingCameraFilter.areMultipleCamerasAvailable()) {
                camera_rotate_button.visibility = View.VISIBLE
            } else {
                camera_rotate_button.visibility = View.GONE
            }

            if(camera.cameraInfo.hasFlashUnit()) {
                camera_torch_button.visibility = View.GONE
            } else {
                camera_torch_button.visibility = View.GONE
            }

            cameraControl = camera.cameraControl

            val aspectRatio = getAspectRatio(camera)
            (capture_window_viewpane.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "${aspectRatio.numerator}:${aspectRatio.denominator}"

            configureTouchToFocus(camera.cameraControl)


        }, ContextCompat.getMainExecutor(this))
    }

    private fun configureTouchToFocus(control : CameraControl) {
        // Listen to tap events on the viewfinder and set them as focus regions
        capture_window_camera_preview.setOnTouchListener(View.OnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@OnTouchListener true
                MotionEvent.ACTION_UP -> {
                    // Get the MeteringPointFactory from PreviewView
                    val factory = capture_window_camera_preview.getMeteringPointFactory()

                    // Create a MeteringPoint from the tap coordinates
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)

                    // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                    val action = FocusMeteringAction.Builder(point).build()

                    // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                    // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                    control.startFocusAndMetering(action)

                    return@OnTouchListener true
                }
                else -> return@OnTouchListener false
            }
        })
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun getAspectRatio(camera: Camera): Rational {
        val cameraId = Camera2CameraInfo.from(camera.cameraInfo).cameraId
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

        if(freezeFrame) {
            freezeFrame()
        }

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                throw exc
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                //replacePhoto(photoFile)
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

    private fun replacePhoto(photoFile: File) {
        photoFile.delete()
        photoFile.createNewFile()
        with(photoFile.outputStream()) {
            val out = this
            with(assets.open("image.jpg")){
                this.copyTo(out)
            }
        }
    }

    private fun freezeFrame() {
        capture_window_freeze_frame.setImageBitmap(capture_window_camera_preview.bitmap)
        capture_window_freeze_frame.visibility = View.VISIBLE
        flash()
    }

    private fun flash() {
        capture_window_flash_frame.visibility = View.VISIBLE
        capture_window_flash_frame.setImageDrawable(ColorDrawable(Color.WHITE))
        capture_window_flash_frame.animate()
                .alpha(0f)
                .setDuration(1000)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        capture_window_flash_frame.visibility = View.INVISIBLE
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

        const val KEY_EVENT_ACTION = "key_event_action"
        const val KEY_EVENT_EXTRA = "key_event_extra"

        //in
        const val EXTRA_RETICLE_RATIO = "windowed_capture_reticle_ratio"
        const val EXTRA_CARD_RATIO = "windowed_capture_card_ratio"
        const val EXTRA_FILE_ROOT = "windowed_capture_file_root"

        //out
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

    var currentCameraSet : MutableList<CameraInfo>? = null

    override fun filter(cameras: MutableList<CameraInfo>): MutableList<CameraInfo> {
        if (currentCameraSet == null) {
            currentCameraSet = LinkedList(cameras)
            return cameras
        } else {
            return mutableListOf(currentCameraSet!!.first())
        }
    }

    fun rotate() {
        if (currentCameraSet != null) {
            Collections.rotate(currentCameraSet,1)
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