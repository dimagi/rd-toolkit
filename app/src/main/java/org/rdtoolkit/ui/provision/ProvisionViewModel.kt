package org.rdtoolkit.ui.provision

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rdtoolkit.component.CaptureConstraints
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.Pamphlet
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile
import org.rdtoolkit.support.model.session.ProvisionMode
import org.rdtoolkit.support.model.session.STATUS
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.support.model.session.setInstructionsViewed
import org.rdtoolkit.util.CombinedLiveData
import java.util.*
import kotlin.collections.HashMap

const val TAG = "ProvisionViewModel"

class ProvisionViewModel(var sessionRepository: SessionRepository,
                         var diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    private lateinit var sessionId: String

    private var sessionConfiguration: MutableLiveData<TestSession.Configuration> = MutableLiveData()

    private var initialConfigFlags: MutableLiveData<Map<String, String>> = MutableLiveData()

    private var metrics = TestSession.Metrics(HashMap())

    private val viewInstructions: MutableLiveData<Boolean>

    private val testProfile: MutableLiveData<RdtDiagnosticProfile> = MutableLiveData()

    var captureConstraints = Transformations.map(CombinedLiveData(testProfile, initialConfigFlags)) {
        CaptureConstraints(it.first.id(), it.second)
    }

    private val testProfileOptions: MutableLiveData<List<RdtDiagnosticProfile>> = MutableLiveData()

    private val debugResolveImmediately: MutableLiveData<Boolean> = MutableLiveData(false)

    private val instructionSets : LiveData<List<Pamphlet>> = Transformations.map(testProfile) {
        profile -> profile?.let{diagnosticsRepository.getReferencePamphlets("reference", listOf(profile.id()))}
    }

    val areInstructionsAvailable : LiveData<Boolean> = Transformations.map(instructionSets) {
        instructions ->  !instructions.isNullOrEmpty()
    }

    private val startAvailable: MutableLiveData<Boolean>

    fun getDebugResolveImmediately() : LiveData<Boolean> {
        return debugResolveImmediately
    }

    fun setDebugResolveImmediately(value : Boolean) {
        if(value != debugResolveImmediately.value) {
            debugResolveImmediately.value = value
        }
    }

    fun updateRequiredInputs(params : MutableSet<String>) {

    }

    fun getInstructionSets() : LiveData<List<Pamphlet>> {
        return instructionSets
    }

    fun setConfig(sessionId: String,
                  config: TestSession.Configuration) {
        this.sessionId = sessionId
        initialConfigFlags.value = config.flags.toMap()
        sessionConfiguration.value = config

        when(config.provisionMode) {
            ProvisionMode.TEST_PROFILE -> testProfile.value = diagnosticsRepository
                    .getTestProfile(config.provisionModeData)
            ProvisionMode.CRITERIA_SET_OR, ProvisionMode.CRITERIA_SET_AND -> {
                val tags = config.provisionModeData.split(" ").toSet()
                val inOrMode = config.provisionMode == ProvisionMode.CRITERIA_SET_OR

                val matchingProfiles = diagnosticsRepository.getMatchingTestProfiles(tags, inOrMode)
                testProfileOptions.value = matchingProfiles.toList()
                testProfile.value = matchingProfiles.first()
            }
            else -> TODO("Implement Result profile mode")
        }
    }

    fun chooseTestProfile(id: String) {
        if(testProfile.value?.id() != id) {
            testProfile.value = diagnosticsRepository.getTestProfile(id)
        }
    }

    fun getProfileOptions() : LiveData<List<RdtDiagnosticProfile>> {
        return testProfileOptions
    }

    fun getSessionConfig() : LiveData<TestSession.Configuration> {
        return sessionConfiguration
    }

    fun getSelectedTestProfile() : LiveData<RdtDiagnosticProfile> {
        return testProfile
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

        var dateTimeToResolve = Date(Date().time + 1000 * profile.timeToResolve())
        var dateTimeToExpire = profile.timeToExpire()?.let { Date(Date().time + 1000 * profile.timeToExpire()) }

        if (debugResolveImmediately.value!!) {
            dateTimeToResolve = Date(Date().time + 5000)
            profile.timeToExpire()?.let {
                dateTimeToExpire = Date(Date().time + 5000 + 1000 * (profile.timeToExpire() - profile.timeToResolve()))
            }
        }

        val session = TestSession(sessionId, STATUS.RUNNING,
                profile.id(),
                sessionConfiguration.value!!,
                Date(),
                dateTimeToResolve,
                dateTimeToExpire,
                null,
                metrics)

        val job = viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.write(session)
        }

        //Cheating for now, this should be extremely fast
        runBlocking {
            job.join();
        }
        return session;
    }

    fun recordInstructionsViewed() {
        metrics.setInstructionsViewed();
    }

    init {
        viewInstructions = MutableLiveData()
        viewInstructions.value = true

        startAvailable = MutableLiveData()
        startAvailable.value = true
    }
}