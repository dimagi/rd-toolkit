package org.rdtoolkit.component.scanwell

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.scanwell.rdtr.model.AlgoData
import com.scanwell.rdtr.reader.RdtReader
import org.finddx.rdt.RDTReader
import org.json.JSONObject
import org.rdtoolkit.component.*
import org.rdtoolkit.component.capture.REQUIREMENT_TAG_CAPTURE_CARD
import org.rdtoolkit.model.diagnostics.*
import java.util.*
import kotlin.collections.HashMap

val COMPONENT_SCANWELL_COVID_PROCESSOR = "classifier_scanwell_c19"

class ScanwellCovidProcessorManifest(val context : Context) : ToolkitComponentManifest<ImageClassifierComponent, ScanwellCovidProcessorConfig> {
    val supportedRdts = mapOf("sd_standard_q_c19" to "sd_standard_q_c19")

    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        if (diagnosticId in supportedRdts) {
            return setOf(COMPONENT_SCANWELL_COVID_PROCESSOR, TAG_READINESS_PRODUCTION)
        } else {
            return setOf()
        }
    }

    override fun getConfigForDiagnostic(diagnosticId: String) : ScanwellCovidProcessorConfig {
        return ScanwellCovidProcessorConfig(supportedRdts[diagnosticId]!!)
    }

    override fun getComponent(config: ScanwellCovidProcessorConfig, sandbox : Sandbox) : ScanwellCovidProcessor {
        return ScanwellCovidProcessor(context, config)
    }

    override fun getCompatibleOutputs(diagnosticId: String) : Set<String> {
        return setOf(CAPTURE_TYPE_CARD)
    }

    override fun getInputRequirements() : Set<String> {
        return setOf(REQUIREMENT_TAG_CAPTURE_CARD)
    }
}

data class ScanwellCovidProcessorConfig(var id: String) : Config {
}

class ScanwellCovidProcessor(private val context : Context, private val config : ScanwellCovidProcessorConfig) : ImageClassifierComponent() {
    override suspend fun processImage(inputResult: ImageCaptureResult, listener : ProcessingListener) {
        listener.onClassifierComplete(HashMap())
    }

}