package org.rdtoolkit.ui.capture

import android.os.CountDownTimer
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile
import org.rdtoolkit.model.session.STATUS
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestReadableState
import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.util.CombinedLiveData
import java.util.*
import kotlin.collections.HashMap

const val TAG = "CaptureViewModel"

class CaptureViewModel(var sessionRepository: SessionRepository,
                       var diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    private var resolveTimer : CountDownTimer? = null

    private var readableTimer : CountDownTimer? = null

    private val testSession : MutableLiveData<TestSession> = MutableLiveData()

    private val testState : MutableLiveData<TestReadableState> = MutableLiveData()

    private val testProfile : LiveData<RdtDiagnosticProfile> = Transformations.map(testSession) {
        session -> diagnosticsRepository.getTestProfile(session.testProfileId)
    }

    private val resolveMillisecondsLeft : MutableLiveData<Long> = MutableLiveData()

    private val readableMillisecondsLeft : MutableLiveData<Long> = MutableLiveData()

    private val rawImageCapturePath : MutableLiveData<String> = MutableLiveData()

    private val testSessionResult : MutableLiveData<TestSession.TestResult> = MutableLiveData()

    private val inCommitMode : MutableLiveData<Boolean> = MutableLiveData(false)

    val sessionCommit = CombinedLiveData<Boolean, TestSession>(inCommitMode, testSession)

    fun getMillisUntilResolved() : LiveData<Long> {
        return resolveMillisecondsLeft
    }

    fun getMillisUntilExpired() : LiveData<Long> {
        return readableMillisecondsLeft
    }

    fun getTestState() : LiveData<TestReadableState> {
        return testState
    }

    fun getTestProfile() : LiveData<RdtDiagnosticProfile> {
        return testProfile
    }

    fun getTestSessionResult() : LiveData<TestSession.TestResult> {
        return testSessionResult
    }

    fun getInCommitMode() : LiveData<Boolean> {
        return inCommitMode
    }

    fun setResultValue(key: String, value: String) {
        val results = testSessionResult.value!!.results
        if (!results.containsKey(key) || results[key] != value) {
            results.put(key, value)
            testSessionResult.value = testSessionResult.value
        }
    }

    fun getRawImageCapturePath() : LiveData<String> {
        return rawImageCapturePath
    }

    fun setRawImageCapturePath(rawImagePath : String) {
        if (rawImageCapturePath.value != rawImagePath) {
            rawImageCapturePath.value = rawImagePath

            val result = testSessionResult.value!!
            result.timeRead = Date()
            result.rawCapturedImageFilePath = rawImagePath
        }
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getTestSession(sessionId)

            testSession.postValue(session)

            if (session.result == null) {
                session.result = TestSession.TestResult(null, null, HashMap())
            }

            testSessionResult.postValue(session.result)

            testState.postValue(session.getTestReadableState())

            startTimersForState(session, diagnosticsRepository.getTestProfile(session.testProfileId))
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
                        // There can be some slight overlap on these so wait an extra hair before
                        // moving on
                        Thread.sleep(200L)
                        testState.postValue(session.getTestReadableState())
                        startTimersForState(session, profile)
                    }
                }.start()
            } else if (session.getTestReadableState() == TestReadableState.READABLE) {
                readableTimer = object : CountDownTimer(session.timeExpired.time - System.currentTimeMillis(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        readableMillisecondsLeft.postValue(millisUntilFinished)
                    }

                    override fun onFinish() {
                        // There can be some slight overlap on these so wait an extra hair before
                        // moving on
                        Thread.sleep(200L)
                        testState.postValue(session.getTestReadableState())
                    }
                }.start()
            }
        }
    }

    fun commitResult() {
        val concreteSession = testSession.value!!
        inCommitMode.value = true
        viewModelScope.launch(Dispatchers.IO) {
            concreteSession.result = testSessionResult.value
            concreteSession.state = STATUS.COMPLETE
            sessionRepository.write(concreteSession)
            testSession.postValue(concreteSession)
            inCommitMode.postValue(false)
        }
    }

    /**
     * Callback called when the ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        resolveTimer?.cancel()
        readableTimer?.cancel()
    }

    init {
        testState.value = TestReadableState.LOADING
    }
}