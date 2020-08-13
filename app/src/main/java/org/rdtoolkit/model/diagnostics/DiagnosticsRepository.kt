package org.rdtoolkit.model.diagnostics

import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.model.session.TestSessionDao
import java.util.HashMap

class DiagnosticsRepository() {
    var builtInSources : MutableMap<String, RdtDiagnosticProfile> = generateBootstrappedDiagnostics()

    fun getTestProfile(id: String) : RdtDiagnosticProfile {
        var profile = builtInSources.get(id)
        if (profile == null) {
            throw Exception("No internal profile for: " + id)
        }
        return profile
    }

    init {

    }
}