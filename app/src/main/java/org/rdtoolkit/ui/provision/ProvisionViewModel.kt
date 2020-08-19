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
import org.rdtoolkit.model.session.ProvisionMode
import org.rdtoolkit.model.session.STATUS
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestSession
import java.util.*

const val TAG = "ProvisionViewModel"

class ProvisionViewModel(var sessionRepository: SessionRepository,
                         var diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    private lateinit var sessionId: String
    private var sessionConfiguration: MutableLiveData<TestSession.Configuration> = MutableLiveData()
    private val viewInstructions: MutableLiveData<Boolean>

    private val testProfile: MutableLiveData<RdtDiagnosticProfile> = MutableLiveData()

    private val instructionsAvailable: MutableLiveData<Boolean>
    private val startAvailable: MutableLiveData<Boolean>

    fun setConfig(sessionId: String,
                  config: TestSession.Configuration) {
        this.sessionId = sessionId
        sessionConfiguration.value = config

        when(config.provisionMode) {
            ProvisionMode.TEST_PROFILE -> testProfile.value = diagnosticsRepository
                    .getTestProfile(config.provisionModeData)
            ProvisionMode.RESULT_PROFILE -> TODO("Implement Result profile mode")
        }
    }

    fun getSessionConfig() : LiveData<TestSession.Configuration> {
        return sessionConfiguration
    }

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

    fun commitSession() : TestSession {
        var profile = testProfile.value!!
        val session = TestSession(sessionId, STATUS.RUNNING,
                profile.id(),
                sessionConfiguration.value!!,
                Date(),
                Date(Date().time +  1000 * profile.timeToResolve()),
                profile.timeToExpire()?.let{Date(Date().time + 1000 * profile.timeToExpire())},
                null)

        val job = viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.write(session)
        }

        //Cheating for now, this should be extremely fast
        runBlocking {
            job.join();
        }
        return session;
    }

    init {
        viewInstructions = MutableLiveData()
        viewInstructions.value = true

        instructionsAvailable = MutableLiveData()
        instructionsAvailable.value = true

        startAvailable = MutableLiveData()
        startAvailable.value = true
    }
}