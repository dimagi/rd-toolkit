package org.rdtoolkit.ui.provision

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile
import org.rdtoolkit.model.session.STATUS
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestSession
import java.util.*

const val TAG = "ProvisionViewModel"

class ProvisionViewModel(var sessionRepository: SessionRepository,
                         var diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {
    private val viewInstructions: MutableLiveData<Boolean>
    val sessionId: String

    private val testProfile: MutableLiveData<RdtDiagnosticProfile> = MutableLiveData()

    private val instructionsAvailable: MutableLiveData<Boolean>
    private val startAvailable: MutableLiveData<Boolean>

    fun getSelectedTestProfile() : LiveData<RdtDiagnosticProfile> {
        return testProfile
    }

    fun getInstructionsAvailable(): LiveData<Boolean> {
        return instructionsAvailable
    }

    fun getStartAvailable(): LiveData<Boolean> {
        return startAvailable
    }

    fun getViewInstructions(): LiveData<Boolean> {
        return viewInstructions
    }

    fun setViewInstructions(setValue: Boolean) {
        if (viewInstructions.value != setValue) {
            viewInstructions.value = setValue
        }
    }

    fun commitSession() : String {
        var profile = testProfile.value!!
        val session = TestSession(sessionId, STATUS.RUNNING,
                profile.id(),
                "Clayton Sims",
                "#423423",
                Date(),
                Date(Date().time +  1000 * profile.timeToResolve()),
                profile.timeToExpire()?.let{Date(Date().time + 1000 * profile.timeToExpire())},
                null)

        val job = viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.insert(session)
        }

        //Cheating for now, this should be extremely fast
        runBlocking {
            job.join();
        }
        return sessionId;
    }

    init {
        viewInstructions = MutableLiveData()
        viewInstructions.value = true
        sessionId = UUID.randomUUID().toString()

        instructionsAvailable = MutableLiveData()
        instructionsAvailable.value = true

        startAvailable = MutableLiveData()
        startAvailable.value = true

        testProfile.value = diagnosticsRepository.getTestProfile("debug_sf_mal_pf_pv")
    }
}