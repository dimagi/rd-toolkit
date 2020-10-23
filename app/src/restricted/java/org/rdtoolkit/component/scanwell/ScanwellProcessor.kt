package org.rdtoolkit.component.scanwell

import android.content.Context
import android.util.Log
import com.scanwell.rdtr.reader.RdtReader
import org.json.JSONObject
import org.rdtoolkit.component.*
import org.rdtoolkit.model.diagnostics.AssetFolioContext
import org.rdtoolkit.model.diagnostics.Folio
import org.rdtoolkit.model.diagnostics.parseFolio
import java.util.*

val COMPONENT_SCANWELL_PROCESSOR = "classifier_scanwell"
val TAG_READINESS_PRODUCTION = "production"

class ScanwellProcessorManifest(val context : Context) : ToolkitComponentManifest<ImageClassifierComponent, ScanwellClassifierConfig> {
    val baseFolioContext = AssetFolioContext("scanwell", context.assets)
    val supportedMeasurements = getMeasurementConfigs(JSONObject(baseFolioContext.spool("config.json")))

    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        if (diagnosticId in supportedMeasurements) {
            return setOf(COMPONENT_SCANWELL_PROCESSOR, TAG_READINESS_PRODUCTION)
        } else {
            return setOf()
        }
    }

    override fun getConfigForDiagnostic(diagnosticId: String) : ScanwellClassifierConfig {
        return ScanwellClassifierConfig(supportedMeasurements[diagnosticId]!!, getFolio(diagnosticId))
    }

    override fun getComponent(config: ScanwellClassifierConfig, sandbox : Sandbox) : ScanwellProcessor {
        return ScanwellProcessor(config)
    }

    override fun getCompatibleOutputs(diagnosticId: String) : Set<String> {
        return setOf(CAPTURE_TYPE_RETICLE)
    }

    private fun getFolio(diagnosticId: String) : Folio {
        val folio = parseFolio(baseFolioContext.spool("folio.json"), baseFolioContext)
        folio.setLocale(Locale.getDefault().language)
        return folio
    }
}

data class ScanwellClassifierConfig(val configData : ScanwellConfigData, val folio : Folio) : Config {
}

class ScanwellProcessor(private val config : ScanwellClassifierConfig) : ImageClassifierComponent() {
    override suspend fun processImage(inputResult: ImageCaptureResult) {
        val reticleResult = inputResult as ReticleCaptureResult

        val reader = RdtReader()
        try {
            val result = reader.runAlgorithmOnImage(
                    "file://" + reticleResult.rawImagePath,
                    config.configData.measurements,
                   reticleResult.reticleOffset)!!

            Log.d(LOG_TAG,"result: ${result.result.rdtResult}");
            Log.d(LOG_TAG,"error code: ${result.errorCode}");
            Log.d(LOG_TAG,"output: ${result.output}");

            val resultCode = result.result.rdtResult.toString()

            if (result.errorCode == 0) {
                if (config.configData.responses.containsKey(resultCode)) {
                    getListener().onClassifierComplete(config.configData.responses[resultCode]!!)
                } else {
                    getListener().onClassifierError("Unexpected response code from classifier: ${resultCode}", null)
                }
            } else {
                getListener().onClassifierError(config.folio.getText("error${result.errorCode}", config.folio.getText("error").format(result.errorCode)), null)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            getListener().onClassifierError("Unexpected Error from Image Processor: ${e.message}", null)
        }
    }

    companion object {
        const val LOG_TAG = "ScanwellProcessor"
    }
}