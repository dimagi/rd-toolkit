package org.rdtoolkit.ui.capture

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestReadableState
import org.rdtoolkit.model.session.TestSession

const val TAG = "CaptureViewModel"

class CaptureViewModel(var sessionRepository: SessionRepository,
                       var diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    private var resolveTimer : CountDownTimer? = null

    private val testSession : MutableLiveData<TestSession> = MutableLiveData()

    private val testState : MutableLiveData<TestReadableState> = MutableLiveData()

    private val testProfile : MutableLiveData<RdtDiagnosticProfile> = MutableLiveData()

    private val resolveMillisecondsLeft : MutableLiveData<Long> = MutableLiveData()

    fun getMillisUntilResolved() : LiveData<Long> {
        return resolveMillisecondsLeft
    }

    fun getTestState() : LiveData<TestReadableState> {
        return testState
    }

    fun getTestProfile() : LiveData<RdtDiagnosticProfile> {
        return testProfile
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.load(sessionId)

            val profile = diagnosticsRepository.getTestProfile(session.testProfileId)

            testProfile.postValue(profile)
            testSession.postValue(session)

            testState.postValue(session.getTestReadableState())

            startTimersForState(session, profile)
        }
    }

    private fun startTimersForState(session : TestSession, profile : RdtDiagnosticProfile) {
        viewModelScope.launch(Dispatchers.Main) {
            if (session.getTestReadableState() == TestReadableState.RESOLVING) {
                resolveTimer = object : CountDownTimer(session.timeResolved.time - System.currentTimeMillis(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        resolveMillisecondsLeft.postValue(millisUntilFinished)
                    }

                    override fun onFinish() {

                    }
                }.start()
            }
        }
    }

    /**
     * Callback called when the ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        resolveTimer?.cancel()
    }


    init {
        testState.value = TestReadableState.LOADING
    }
}