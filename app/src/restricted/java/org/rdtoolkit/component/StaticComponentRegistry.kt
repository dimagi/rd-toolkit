package org.rdtoolkit.component

import android.content.Context
import org.rdtoolkit.component.scanwell.DiatomicProcessorManifest
import org.rdtoolkit.component.scanwell.ScanwellProcessorManifest

class StaticComponentRegistry(val context : Context) {
    fun bootstrap(repository : ComponentRepository) {
        repository.registerClassifierComponent(DiatomicProcessorManifest(context))
    }
}
