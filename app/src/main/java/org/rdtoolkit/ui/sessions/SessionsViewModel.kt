package org.rdtoolkit.ui.sessions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestSession

class SessionsViewModel(var sessionRepository: SessionRepository,
                       var diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    val testSessions = MutableLiveData<List<TestSession>>()

    fun getDiagnosticsRepo() : DiagnosticsRepository {
        return diagnosticsRepository
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            testSessions.postValue(sessionRepository.loadSessions())
        }
    }
}