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
import org.rdtoolkit.model.diagnostics.*
import java.util.*

val COMPONENT_DIATOMIC_PROCESSOR = "classifier_diatomic"

class DiatomicProcessorManifest(val context : Context) : ToolkitComponentManifest<ImageClassifierComponent, DiatomicClassifierConfig> {
    val baseFolioContext = AssetFolioContext("diatomic", context.assets)
    val supportedRdts = mapOf("carestart_mal_pf_pv" to RDTReader.RDTType.CARESTART,
                                "sd_bioline_mal_pf_pv" to RDTReader.RDTType.SDBIO,
                                "firstresponse_mal_pf" to RDTReader.RDTType.FIRSTRESPONSE)

    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        if (diagnosticId in supportedRdts) {
            return setOf(COMPONENT_DIATOMIC_PROCESSOR, TAG_READINESS_PRODUCTION)
        } else {
            return setOf()
        }
    }

    override fun getConfigForDiagnostic(diagnosticId: String) : DiatomicClassifierConfig {
        return DiatomicClassifierConfig(supportedRdts[diagnosticId]!!, getFolio(diagnosticId))
    }

    override fun getComponent(config: DiatomicClassifierConfig, sandbox : Sandbox) : DiatomicProcessor {
        return DiatomicProcessor(context, config)
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

data class DiatomicClassifierConfig(val rdt : RDTReader.RDTType, val folio : Folio) : Config {

    fun getResultsMap(resultCode: RDTReader.ResultCode) : Map<String, String> {
        when (rdt) {
            in pfPvTests -> {
                return when (resultCode) {
                    RDTReader.ResultCode.NEGATIVE -> mapOf(DIAG_PF to DIAG_PF_NEG, DIAG_PV to DIAG_PV_NEG)
                    RDTReader.ResultCode.FIRSTLINE -> mapOf(DIAG_PF to DIAG_PF_NEG, DIAG_PV to DIAG_PV_POS)
                    RDTReader.ResultCode.SECONDLINE -> mapOf(DIAG_PF to DIAG_PF_POS, DIAG_PV to DIAG_PV_NEG)
                    RDTReader.ResultCode.BOTHLINES -> mapOf(DIAG_PF to DIAG_PF_POS, DIAG_PV to DIAG_PV_POS)
                    RDTReader.ResultCode.INVALID -> mapOf(DIAG_PF to UNIVERSAL_CONTROL_FAILURE, DIAG_PV to UNIVERSAL_CONTROL_FAILURE)
                    RDTReader.ResultCode.INDETERMINATE -> mapOf(DIAG_PF to UNIVERSAL_CONTROL_FAILURE, DIAG_PV to UNIVERSAL_CONTROL_FAILURE)
                    else -> throw Exception("Invalid Result Code: $resultCode")
                }
            }
            in pfTests -> {
                return when (resultCode) {
                    RDTReader.ResultCode.NEGATIVE -> mapOf(DIAG_PF to DIAG_PF_NEG)
                    RDTReader.ResultCode.FIRSTLINE -> mapOf(DIAG_PF to DIAG_PF_POS)
                    RDTReader.ResultCode.INVALID -> mapOf(DIAG_PF to UNIVERSAL_CONTROL_FAILURE)
                    RDTReader.ResultCode.INDETERMINATE -> mapOf(DIAG_PF to UNIVERSAL_CONTROL_FAILURE)
                    else -> throw Exception("Invalid Result Code: $resultCode")
                }
            }
            else -> {
                throw Exception("Unrecognized RDT Type $rdt")
            }
        }
    }

    companion object {
        private val pfPvTests = setOf(RDTReader.RDTType.CARESTART, RDTReader.RDTType.SDBIO)
        private val pfTests = setOf(RDTReader.RDTType.FIRSTRESPONSE)
    }
}

class DiatomicProcessor(private val context : Context, private val config : DiatomicClassifierConfig) : ImageClassifierComponent() {
    override suspend fun processImage(inputResult: ImageCaptureResult, listener : ProcessingListener) {

        val reticleResult = inputResult as ReticleCaptureResult

        val decoder = BitmapFactory.Options()
        decoder.inPreferredConfig = Bitmap.Config.ARGB_8888;

        val image = BitmapFactory.decodeFile(reticleResult.rawImagePath, decoder)
        Log.i(LOG_TAG, "Initializing ${image.height} x ${image.width} reader for test ${config.rdt}");


        RDTReader.init(context, config.rdt, image.height, image.width)

        val result = RDTReader.readRDT(image)
        if (result.errorCode == null || result.resultCode == null) {
            throw Exception("Invalid Result Data from Scanner")
        }
        Log.i(LOG_TAG, String.format("RDTReader::findRdt(): %s(%d) Result %s(%d)", result.errorCode, result.errorCode.code, result.resultCode, result.resultCode.code));

        if (result.errorCode == RDTReader.ErrorCode.SUCCESS) {
            listener.onClassifierComplete(config.getResultsMap(result.resultCode))
        } else {
            listener.onClassifierError(config.folio.getText("error-${result.errorCode.code}", config.folio.getText("error-generic")).format(result.errorCode.toString()), null)
        }
    }

    companion object {
        const val LOG_TAG = "DiatomicProcessor"
    }
}