package org.rdtoolkit.component

class ComponentRepository() {

    fun getCaptureComponentForTest(testProfileId: String) : TestImageCaptureComponent {
        return DefaultImageCaptureComponent()
    }

    init {

    }
}