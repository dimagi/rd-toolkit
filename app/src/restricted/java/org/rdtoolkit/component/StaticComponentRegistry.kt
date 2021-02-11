package org.rdtoolkit.component

import android.content.Context
import org.rdtoolkit.component.scanwell.DiatomicProcessorManifest
import org.rdtoolkit.component.scanwell.ScanwellCovidProcessor
import org.rdtoolkit.component.scanwell.ScanwellCovidProcessorManifest
import org.rdtoolkit.component.scanwell.ScanwellProcessorManifest

class StaticComponentRegistry(val context : Context) {
    fun bootstrap(repository : ComponentRepository) {
        repository.registerClassifierComponent(DiatomicProcessorManifest(context))
        //repository.registerClassifierComponent(ScanwellCovidProcessorManifest(context))
    }
}
