package org.rdtoolkit.ui.capture

import android.os.CountDownTimer
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.Pamphlet
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile
import org.rdtoolkit.model.session.*
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

    private var classifierMode : ClassifierMode? = null

    private var processingStateValue : MutableLiveData<ProcessingState> = MutableLiveData(ProcessingState.PRE_CAPTURE)

    private var processingErrorValue : MutableLiveData<Pair<String, Pamphlet?>> = MutableLiveData()

    val allowOverrideValue : MutableLiveData<Boolean> = MutableLiveData()

    fun getExpireOverrideChecked() : LiveData<Boolean> {
        return allowOverrideValue
    }

    fun setExpireOverrideChecked(isChecked : Boolean) {
        if (isChecked != allowOverrideValue.value) {
            allowOverrideValue.value = isChecked
        }
    }

    fun getProcessingError() : LiveData<Pair<String, Pamphlet?>> {
        return processingErrorValue
    }

    fun getProcessingState() : LiveData<ProcessingState> {
        return processingStateValue
    }

    fun getTestSession() : LiveData<TestSession> {
        return testSession
    }

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


            result.results.clear()
            result.classifierResults.clear()
            processingErrorValue.value = null
            testSessionResult.value = testSessionResult.value

            if (classifierMode != ClassifierMode.NONE) {
                processingStateValue.value = ProcessingState.PROCESSING
            } else {
                processingStateValue.value = ProcessingState.COMPLETE
            }
        }
    }

    fun disableProcessing() {
        if (classifierMode != ClassifierMode.NONE) {
            classifierMode = ClassifierMode.NONE
            if(processingStateValue.value == ProcessingState.PROCESSING ||
                    processingStateValue.value == ProcessingState.ERROR) {
                processingStateValue.value = ProcessingState.COMPLETE
            }
        }
    }


    fun setClassifierResults(classifierResults : MutableMap<String, String>) {
        testSessionResult.value!!.classifierResults.putAll(classifierResults)
        processingStateValue.value = ProcessingState.COMPLETE
    }

    fun setProcessingError(error: String, details: Pamphlet?) {
        processingStateValue.value = ProcessingState.ERROR
        this.processingErrorValue.value = Pair(error, details)
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val session = sessionRepository.getTestSession(sessionId)

            testSession.postValue(session)

            if (session.result == null) {
                session.result = TestSession.TestResult(null, null, HashMap(), HashMap())
            }

            testSessionResult.postValue(session.result)

            testState.postValue(session.getTestReadableState())

            classifierMode = session.configuration.classifierMode
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

    fun setExpirationOverriden() {
        //TODO: Move this into a flag on the session
        testState.value = TestReadableState.READABLE
    }

    fun commitResult() {
        val concreteSession = testSession.value!!
        inCommitMode.value = true
        viewModelScope.launch(Dispatchers.IO) {
            if (testState.value == TestReadableState.EXPIRED && testSessionResult.value!!.results.isEmpty()) {
                //TODO: this should really be reflected in the test state somewhere
                concreteSession.result = null
            } else {
                concreteSession.result = testSessionResult.value
            }
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

enum class ProcessingState {
    PRE_CAPTURE,
    PROCESSING,
    COMPLETE,
    ERROR
}