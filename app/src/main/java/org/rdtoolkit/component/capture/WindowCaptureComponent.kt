package org.rdtoolkit.component.capture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.Window
import androidx.camera.core.AspectRatio
import org.rdtoolkit.component.*
import java.io.File

val COMPONENT_WINDOWED_CAPTURE = "capture_windowed"

class WindowCaptureComponentManifest : ToolkitComponentManifest<TestImageCaptureComponent, WindowCaptureConfig> {

    private val availableCaptureConfigs :  Map<String, WindowCaptureConfig>

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

    override fun getComponent(config: WindowCaptureConfig) : TestImageCaptureComponent {
        return WindowCaptureComponent(config)
    }

    init {
        availableCaptureConfigs = mapOf (
                "debug_mal_pf_pv" to WindowCaptureConfig("7:2")
                ,"debug_sf_mal_pf_pv" to WindowCaptureConfig("7:2")
                ,"sd_bioline_mal_pf_pv" to WindowCaptureConfig("7:2")
                ,"carestart_mal_pf_pv" to WindowCaptureConfig("6:2")
                ,"firstresponse_mal_pf_pv" to WindowCaptureConfig("7:2")
        )
    }
}

data class WindowCaptureConfig(val cassetteAspectRatio : String): Config


class WindowCaptureComponent(private val config : WindowCaptureConfig) :
        TestImageCaptureComponent(), ActivityLifecycleComponent {
    var returnPhotoPath : String? = null

    override fun triggerCallout(activity: Activity) {
        val calloutIntent = Intent(activity, WindowCaptureActivity::class.java)
        calloutIntent.putExtra(EXTRA_RETICLE_RATIO, config.cassetteAspectRatio)

        activity.startActivityForResult(calloutIntent, componentInterfaceId!!)
    }

    override fun processIntentCallback(requestCode : Int,
                                       resultCode : Int,
                                       data : Intent?) {
        if (requestCode == componentInterfaceId && resultCode == Activity.RESULT_OK) {

            val returnPhoto = File(data!!.getStringExtra(WindowCaptureActivity.EXTRA_CROPPED_IMAGE))
            if (returnPhoto.exists()) {
                returnPhotoPath = returnPhoto.absolutePath
                listener!!.testImageCaptured(returnPhotoPath!!)
            }
        }
    }


    override fun getResultImage(): String {
        return returnPhotoPath!!
    }

    override fun captureImage() {
        triggerCallout(activity!!)
    }
}