package org.rdtoolkit.component.capture

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import org.rdtoolkit.component.*
import org.rdtoolkit.component.capture.WindowCaptureActivity.Companion.EXTRA_FILE_ROOT
import org.rdtoolkit.component.capture.WindowCaptureActivity.Companion.EXTRA_RETICLE_RATIO
import java.io.File

val COMPONENT_WINDOWED_CAPTURE = "capture_windowed"

class WindowCaptureComponentManifest : ToolkitComponentManifest<TestImageCaptureComponent, WindowCaptureConfig> {

    private val availableCaptureConfigs :  Map<String, WindowCaptureConfig> = mapOf (
            "debug_mal_pf_pv" to WindowCaptureConfig("7:2")
            ,"debug_sf_mal_pf_pv" to WindowCaptureConfig("6:2")
            ,"sd_bioline_mal_pf_pv" to WindowCaptureConfig("7:2")
            ,"carestart_mal_pf_pv" to WindowCaptureConfig("6:2")
            ,"firstresponse_mal_pf" to WindowCaptureConfig("7:2")
            ,"sd_standard_q_mal_pf_ag" to WindowCaptureConfig("7:2")
            ,"sd_bioline_mal_pf" to WindowCaptureConfig("7:2")
    )

    private val defaultCaptureConfig = WindowCaptureConfig("7:2")

    override fun getValue() : Int {
        return VALUE_PREFERRED
    }

    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        if (diagnosticId in availableCaptureConfigs) {
            return setOf(COMPONENT_WINDOWED_CAPTURE, TAG_READINESS_PRODUCTION)
        } else {
            return setOf(COMPONENT_WINDOWED_CAPTURE, TAG_READINESS_AVAILABLE)
        }
    }

    override fun getConfigForDiagnostic(diagnosticId: String) : WindowCaptureConfig {
        if (diagnosticId in availableCaptureConfigs) {
            return availableCaptureConfigs[diagnosticId]!!
        } else {
            return defaultCaptureConfig
        }
    }

    override fun getComponent(config: WindowCaptureConfig, sandbox : Sandbox) : TestImageCaptureComponent {
        return WindowCaptureComponent(config, sandbox)
    }

    override fun getCompatibleOutputs(diagnosticId: String) : Set<String> {
        return setOf(CAPTURE_TYPE_PLAIN, CAPTURE_TYPE_RETICLE)
    }
}

data class WindowCaptureConfig(val cassetteAspectRatio : String): Config


class WindowCaptureComponent(private val config : WindowCaptureConfig, private val sandbox: Sandbox) :
        TestImageCaptureComponent(), ActivityLifecycleComponent {
    var croppedPhotoPath : String? = null
    var rawPhotoPath : String? = null
    var cropWindow : Rect? = null

    fun triggerCallout(activity: Activity) {
        val calloutIntent = Intent(activity, WindowCaptureActivity::class.java)
        calloutIntent.putExtra(EXTRA_RETICLE_RATIO, config.cassetteAspectRatio)
        calloutIntent.putExtra(EXTRA_FILE_ROOT, sandbox.getFileRoot().absolutePath)

        activity.startActivityForResult(calloutIntent, componentInterfaceId!!)
    }

    override fun processIntentCallback(requestCode : Int,
                                       resultCode : Int,
                                       data : Intent?) {
        if (requestCode == componentInterfaceId && resultCode == Activity.RESULT_OK) {

            val croppedPhoto = File(data!!.getStringExtra(WindowCaptureActivity.EXTRA_CROPPED_IMAGE))
            if (croppedPhoto.exists()) {
                croppedPhotoPath = croppedPhoto.absolutePath
                rawPhotoPath = File(data!!.getStringExtra(WindowCaptureActivity.EXTRA_ORIGINAL_IMAGE)).absolutePath
                cropWindow = data.getParcelableExtra(WindowCaptureActivity.EXTRA_RETICLE_RECT)
                getListener().testImageCaptured(getResultImage())
            }
        }
    }


    override fun getResultImage(): ReticleCaptureResult {
        return ReticleCaptureResult(rawPhotoPath!!, croppedPhotoPath!!, cropWindow!!)
    }

    override fun captureImage() {
        triggerCallout(activity!!)
    }
}