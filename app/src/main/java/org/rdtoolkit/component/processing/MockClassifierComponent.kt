package org.rdtoolkit.component.processing

import kotlinx.coroutines.delay
import org.rdtoolkit.component.Config
import org.rdtoolkit.component.ImageCaptureResult
import org.rdtoolkit.component.ImageClassifierComponent
import org.rdtoolkit.component.ToolkitComponentManifest

val COMPONENT_WINDOWED_CAPTURE = "classifier_mock"
val TAG_READINESS_MOCK = "production"

class MockClassifierManifest : ToolkitComponentManifest<ImageClassifierComponent, MockClassifierConfig> {

    override fun getTagsForDiagnostic(diagnosticId: String) : Set<String> {
        return setOf(COMPONENT_WINDOWED_CAPTURE, TAG_READINESS_MOCK)
    }

    override fun getConfigForDiagnostic(diagnosticId: String) : MockClassifierConfig {
        return MockClassifierConfig(diagnosticId)
    }

    override fun getComponent(config: MockClassifierConfig) : MockClassifierComponent {
        return MockClassifierComponent(config)
    }
}

data class MockClassifierConfig(val diagnosticId: String) : Config


class MockClassifierComponent(private val config : MockClassifierConfig) : ImageClassifierComponent() {
    var failCount = 0

    override suspend fun processImage(inputResult: ImageCaptureResult) {
        delay(3000L)
        if(failCount  < 1) {
            failCount++
            this.listener!!.onClassifierError("Failed to process $failCount times", null)
        } else {
            this.listener!!.onClassifierComplete(mapOf<String,String>().toMutableMap())
        }
    }
}