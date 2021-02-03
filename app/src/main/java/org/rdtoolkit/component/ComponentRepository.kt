package org.rdtoolkit.component

import android.content.Context
import org.rdtoolkit.component.capture.CardWindowCaptureManifest
import org.rdtoolkit.component.capture.WindowCaptureComponentManifest
import org.rdtoolkit.component.processing.MockClassifierManifest
import org.rdtoolkit.support.model.session.FLAG_CAPTURE_PARAMS
import org.rdtoolkit.support.model.session.FLAG_CAPTURE_REQUIREMENTS

class ComponentRepository(context: Context) {

    val imageCaptureManifests = HashSet<ToolkitComponentManifest<TestImageCaptureComponent, Any>>()

    val classifierManifests = HashSet<ToolkitComponentManifest<ImageClassifierComponent, Any>>()

    fun registerCaptureComponent(manifest: ToolkitComponentManifest<TestImageCaptureComponent, *>){
        imageCaptureManifests.add(manifest as ToolkitComponentManifest<TestImageCaptureComponent, Any>)
    }

    fun registerClassifierComponent(manifest: ToolkitComponentManifest<ImageClassifierComponent, *>){
        classifierManifests.add(manifest as ToolkitComponentManifest<ImageClassifierComponent, Any>)
    }

    fun getCaptureComponentForTest(testProfileId: String, captureConstraints: CaptureConstraints, compatibleCaptureFormats : List<String>?, sandbox: Sandbox) : TestImageCaptureComponent {
        val matchingComponents =
                imageCaptureManifests.filter { it.getTagsForDiagnostic(testProfileId).containsAll(captureConstraints.getSessionRequiredTags()) }
                        .filter { compatibleCaptureFormats == null || it.getCompatibleOutputs(testProfileId).intersect(compatibleCaptureFormats).isNotEmpty() }
                        .sortedByDescending { it.getValue() }
        if (matchingComponents.isEmpty()) {
            throw Exception("No matching Capture Components available!")
        }
        val component = matchingComponents.first()
        return component.getComponent(component.getConfigForDiagnostic(testProfileId), sandbox)
    }

    fun getClassifierComponentForTest(captureConstraints: CaptureConstraints, sandbox: Sandbox) : ImageClassifierComponent? {
        val matchingComponents =
                classifierManifests.filter { it.getTagsForDiagnostic(captureConstraints.testProfileId).containsAll(captureConstraints.getSessionRequiredTags()) }.sortedByDescending { it.getValue() }
        if (matchingComponents.isEmpty()) {
            return null
        }
        val component = matchingComponents.first()
        return component.getComponent(component.getConfigForDiagnostic(captureConstraints.testProfileId), sandbox)
    }

    /**
     * Based on the test toolkit capture planner, identify tags which may change how the test
     * is captured for user input.
     */
    fun getParameterInputs(constraints : CaptureConstraints) : Set<String> {
        //Get all classifiers available for the test provided
        val matchingComponents = classifierManifests.filter { it.getTagsForDiagnostic(constraints.testProfileId).containsAll(constraints.getSessionRequiredTags()) }.sortedByDescending { it.getValue() }
        return if (matchingComponents.isEmpty()) {
            emptySet()
        } else {
            matchingComponents.first().getInputRequirements().minus(constraints.getSessionParameterTags())
        }
    }

    init {
        registerCaptureComponent(PlainCameraComponentManifest())
        registerCaptureComponent(WindowCaptureComponentManifest())
        registerCaptureComponent(CardWindowCaptureManifest())
        registerClassifierComponent(MockClassifierManifest())

        //NOTE: I don't love this because it's pretty one-to-one
        StaticComponentRegistry(context).bootstrap(this)
    }
}

class CaptureConstraints(val testProfileId : String, val sessionFlags : Map<String, String> = mapOf()) {
    fun getSessionRequiredTags() : Set<String> {
        sessionFlags[FLAG_CAPTURE_REQUIREMENTS]?.let {
            return it.split(" ").toSet()
        }
        return setOf("production")
    }

    fun getSessionParameterTags() : Set<String> {
        sessionFlags[FLAG_CAPTURE_PARAMS]?.let {
            return it.split(" ").toSet()
        }
        return emptySet()
    }
}
