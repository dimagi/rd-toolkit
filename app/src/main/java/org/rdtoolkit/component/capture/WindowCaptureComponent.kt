package org.rdtoolkit.component.capture

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import org.rdtoolkit.component.*
import org.rdtoolkit.component.capture.WindowCaptureActivity.Companion.EXTRA_CARD_RATIO
import org.rdtoolkit.component.capture.WindowCaptureActivity.Companion.EXTRA_FILE_ROOT
import org.rdtoolkit.component.capture.WindowCaptureActivity.Companion.EXTRA_RETICLE_RATIO
import java.io.File

const val COMPONENT_WINDOWED_CAPTURE = "capture_windowed"
val COMPONENT_WINDOWED_CAPTURE_CARD = "capture_windowed_card"

val REQUIREMENT_TAG_CAPTURE_CARD = "tag_has_capture_card"

class WindowCaptureComponentManifest : ToolkitComponentManifest<TestImageCaptureComponent, WindowCaptureConfig> {

    private val availableCaptureConfigs :  Map<String, WindowCaptureConfig> = mapOf (
            "debug_mal_pf_pv" to WindowCaptureConfig("7:2")
            ,"debug_sf_mal_pf_pv" to WindowCaptureConfig("6:2")
            ,"sd_bioline_mal_pf_pv" to WindowCaptureConfig("7:2")
            ,"carestart_mal_pf_pv" to WindowCaptureConfig("6:2")
            ,"firstresponse_mal_pf" to WindowCaptureConfig("7:2")
            ,"sd_standard_q_mal_pf_ag" to WindowCaptureConfig("7:2")
            ,"sd_bioline_mal_pf" to WindowCaptureConfig("7:2")
            ,"meriscreen_pf_pv" to WindowCaptureConfig("7:2")
            ,"sd_standard_q_c19" to WindowCaptureConfig("7:2")
            ,"abbott_panbio_c19_nasal" to WindowCaptureConfig("7:2")
            ,"abbott_panbio_c19_nasopharyngeal" to WindowCaptureConfig("7:2")
            ,"premier_medical_sure_status_c19" to WindowCaptureConfig("7:2")
            ,"generic_c19_fifteen" to WindowCaptureConfig("7:2")
            ,"generic_c19_twenty" to WindowCaptureConfig("7:2")
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

class CardWindowCaptureManifest : ToolkitComponentManifest<TestImageCaptureComponent, WindowCaptureConfig> {

    private val availableCaptureConfigs :  Map<String, WindowCaptureConfig> = mapOf (
            "sd_standard_q_c19" to WindowCaptureConfig("7:2", "5:3")
    )

    private val defaultCaptureConfig = WindowCaptureConfig("7:2", "5:3")

    override fun getValue() : Int {
        return VALUE_POSITIVE
    }

    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        if (diagnosticId in availableCaptureConfigs) {
            return setOf(COMPONENT_WINDOWED_CAPTURE_CARD, TAG_READINESS_PRODUCTION)
        } else {
            return setOf(COMPONENT_WINDOWED_CAPTURE_CARD, TAG_READINESS_AVAILABLE)
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
        return setOf(CAPTURE_TYPE_CARD)
    }

    override fun getInputRequirements() : Set<String> {
        return setOf(REQUIREMENT_TAG_CAPTURE_CARD)
    }
}

data class WindowCaptureConfig(val cassetteAspectRatio : String, val cardAspectRatio : String? = null) : Config {
    override fun toString() : String {
        return "${this.javaClass.toString()}|$cassetteAspectRatio|$cardAspectRatio"
    }
}


class WindowCaptureComponent(private val config : WindowCaptureConfig, private val sandbox: Sandbox) :
        TestImageCaptureComponent(), ActivityLifecycleComponent {
    var croppedPhotoPath : String? = null
    var rawPhotoPath : String? = null
    var cropWindow : Rect? = null

    fun triggerCallout(activity: Activity) {
        val calloutIntent = Intent(activity, WindowCaptureActivity::class.java)
        calloutIntent.putExtra(EXTRA_RETICLE_RATIO, config.cassetteAspectRatio)

        if (config.cardAspectRatio != null) {
            calloutIntent.putExtra(EXTRA_CARD_RATIO, config.cardAspectRatio)
        }

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

    override fun toString() : String {
        return this.javaClass.name + "|" + config.toString()
    }
}