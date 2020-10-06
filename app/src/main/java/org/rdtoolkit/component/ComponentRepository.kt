package org.rdtoolkit.component

import org.rdtoolkit.component.capture.WindowCaptureComponentManifest

class ComponentRepository() {

    val imageCaptureManifests = HashSet<ToolkitComponentManifest<TestImageCaptureComponent, Any>>()

    fun registerCaptureComponent(manifest: ToolkitComponentManifest<TestImageCaptureComponent, *>){
        imageCaptureManifests.add(manifest as ToolkitComponentManifest<TestImageCaptureComponent, Any>)
    }

    fun getCaptureComponentForTest(testProfileId: String, tags : Set<String>) : TestImageCaptureComponent {
        val matchingComponents = imageCaptureManifests.filter { it.getTagsForDiagnostic(testProfileId).containsAll(tags) }.sortedByDescending { it.getValue() }
        if (matchingComponents.size == 0) {
            throw Exception("No matching Capture Components available!")
        }
        val component = matchingComponents.first()
        return component.getComponent(component.getConfigForDiagnostic(testProfileId))
    }

    init {
        registerCaptureComponent(PlainCameraComponentManifest())
        registerCaptureComponent(WindowCaptureComponentManifest())
    }
}