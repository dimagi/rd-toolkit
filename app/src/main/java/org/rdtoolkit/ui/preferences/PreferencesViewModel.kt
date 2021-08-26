package org.rdtoolkit.ui.preferences

import androidx.lifecycle.ViewModel
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository

class PreferencesViewModel(var diagnosticsRepository: DiagnosticsRepository) : ViewModel() {
    fun getDiagnosticsRepo() : DiagnosticsRepository {
        return diagnosticsRepository
    }
}