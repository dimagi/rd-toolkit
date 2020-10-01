package org.rdtoolkit.component

import org.rdtoolkit.component.capture.WindowCaptureActivity
import org.rdtoolkit.component.capture.WindowCaptureComponent

class ComponentRepository() {

    fun getCaptureComponentForTest(testProfileId: String) : TestImageCaptureComponent {
        if ("debug_mal_pf_pv".equals(testProfileId) || "debug_sf_mal_pf_pv".equals(testProfileId)) {
            return WindowCaptureComponent()
        }
        return DefaultImageCaptureComponent()
    }

    init {

    }
}