package org.rdtoolkit.ui.capture

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.rdtoolkit.model.diagnostics.DiagnosticsRepository
import org.rdtoolkit.model.diagnostics.Pamphlet
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile
import org.rdtoolkit.model.session.AppRepository
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.support.model.session.*
import org.rdtoolkit.util.CombinedLiveData
import java.util.*
import kotlin.collections.HashMap

const val TAG = "CaptureViewModel"

class CaptureViewModel(var sessionRepository: SessionRepository,
                       var diagnosticsRepository: DiagnosticsRepository,
                       var appRepository: AppRepository
) : ViewModel() {

    private val testSession : MutableLiveData<TestSession> = MutableLiveData()
    private val testState : MutableLiveData<TestReadableState> = MutableLiveData()

    val sessionIsInvalid : MutableLiveData<Boolean> = MutableLiveData(false)

    private val testProfile : LiveData<RdtDiagnosticProfile> = Transformations.map(testSession) {
        session -> diagnosticsRepository.getTestProfile(session.testProfileId)
    }

    private var resolveTimer : CountDownTimer? = null
    private var readableTimer : CountDownTimer? = null
    private val resolveMillisecondsLeft : MutableLiveData<Long> = MutableLiveData()
    private val readableMillisecondsLeft : MutableLiveData<Long> = MutableLiveData()

    private val inCommitMode : MutableLiveData<Boolean> = MutableLiveData(false)
    val sessionCommit = CombinedLiveData<Boolean, TestSession>(inCommitMode, testSession)

    private val overrideExpirationValue : MutableLiveData<Boolean> = MutableLiveData()

    private val numberOfFailedCaptures = MutableLiveData(0)

    private val rawImageCapturePath : MutableLiveData<String> = MutableLiveData()
    private val testSessionResult : MutableLiveData<TestSession.TestResult> = MutableLiveData()

    private var classifierMode : ClassifierMode? = null

    private var processingStateValue : MutableLiveData<ProcessingState> = MutableLiveData(ProcessingState.PRE_CAPTURE)

    private var processingErrorValue : MutableLiveData<Pair<String, Pamphlet?>> = MutableLiveData()

    private var totalNumberOfCaptureAttempts = 0

    val secondaryCaptureCompatible = MutableLiveData<Boolean>(true)

    val secondaryCaptureEnabled = Transformations.map(CombinedLiveData(testSession, secondaryCaptureCompatible)) {
        it.first.configuration.wasSecondaryCaptureRequested() && it.second
    }

    val secondaryImageCapturePath : MutableLiveData<String> = MutableLiveData()

    val secondaryImageCaptured : MutableLiveData<Boolean> = MutableLiveData(false)

    val jobAidPamphlets : LiveData<List<Pamphlet>> = Transformations.map(testProfile) {
        profile -> profile?.let{diagnosticsRepository.getReferencePamphlets("interpret", listOf(profile.id()))}
    }

    val jobAidAvailable : LiveData<Boolean> = Transformations.map(jobAidPamphlets) {
        instructions ->  !instructions.isNullOrEmpty()
    }

    private val earlyReadsEnabled : LiveData<Boolean> =  Transformations.map(testSession) {
        FLAG_VALUE_SET != it.configuration.flags[FLAG_SESSION_NO_EARLY_READS]
    }

    private val timerDisclaimerAcknowledged : MutableLiveData<Boolean> = MutableLiveData()

    val timerSkipDisclaimerAvailable : LiveData<Boolean> = Transformations.map(CombinedLiveData(earlyReadsEnabled,timerDisclaimerAcknowledged)) {
        it.first && !it.second
    }

    val timerSkipAvailable : LiveData<Boolean> = Transformations.map(CombinedLiveData(earlyReadsEnabled,timerDisclaimerAcknowledged)) {
        it.first && it.second
    }

    val captureIsIncomplete = Transformations.map(CombinedLiveData<TestSession.TestResult, ProcessingState>(testSessionResult,processingStateValue)) {
        combinedData -> combinedData.first.mainImage == null || !(combinedData.second == ProcessingState.COMPLETE || combinedData.second == ProcessingState.PROCESSING || combinedData.second == ProcessingState.SKIPPED)
    }

    val testCapturedLate = Transformations.map(CombinedLiveData<TestSession.TestResult, TestReadableState>(testSessionResult,testState)) {
        combinedData -> (testSession.value!!.timeExpired != null && combinedData.first.timeRead != null &&
            (combinedData.first.timeRead!!.after(testSession.value!!.timeExpired)) || combinedData.first.timeRead == null && combinedData.second == TestReadableState.EXPIRED)
    }

    val sessionStateInputs = CombinedLiveData(testState, captureIsIncomplete)

    val secondaryCaptureRelevant = Transformations.map(CombinedLiveData(testState, secondaryCaptureEnabled)) {
        it.first != TestReadableState.EXPIRED && it.second
    }

    val secondaryCaptureAvailableOrIrrelevant = Transformations.map(CombinedLiveData(secondaryCaptureRelevant, testSessionResult)) {
        testProfile.value!!.isResultSetComplete(it.second)
    }

    val secondaryCaptureFinishedOrIrrelevant= Transformations.map(CombinedLiveData(secondaryCaptureAvailableOrIrrelevant, CombinedLiveData(secondaryCaptureRelevant,secondaryImageCaptured))) {
        it.first && (!it.second.first || it.second.second)
    }

    val recordAvailable = Transformations.map(CombinedLiveData(sessionStateInputs, secondaryCaptureFinishedOrIrrelevant)) {
        if (it.first.first === TestReadableState.EXPIRED && it.first.second) {
            true
        } else {
            it.second
        }
    }

    val permitCaptureOverride = Transformations.map(CombinedLiveData<Int, ProcessingState>(numberOfFailedCaptures,processingStateValue)) {
        it.first >= 3 && it.second == ProcessingState.ERROR
    }

    private val needsWorkCheck = Transformations.map(CombinedLiveData<TestSession, TestSession.TestResult>(testSession, testSessionResult)) {
        it.first.configuration.classifierMode == ClassifierMode.CHECK_YOUR_WORK &&
            it.second.classifierResults.isNotEmpty() && it.second.results.isNotEmpty() &&
            areClassifierOutcomesDifferent(it.second)
    }

    private val workChecked : MutableLiveData<Boolean> = MutableLiveData(false)

    val requireWorkCheck = Transformations.map(CombinedLiveData(needsWorkCheck, workChecked)) {
        it.first && !it.second
    }

    val secondaryRequestAcknowledged = MutableLiveData(false)

    private fun recordCaptureAttempt() {
        totalNumberOfCaptureAttempts++
        testSession.value!!.metrics.setCaptureAttempts(totalNumberOfCaptureAttempts)
    }

    fun recordJobAidViewed() {
        testSession.value!!.metrics.setJobAidViewed();
    }

    private fun areClassifierOutcomesDifferent(it: TestSession.TestResult): Boolean {
        for (entry in it.results) {
            if (!it.classifierResults[entry.key].equals(entry.value)) {
                return true
            }
        }
        return false
    }

    fun setWorkCheckTriggered() {
        workChecked.value = true
    }

    fun getExpireOverrideChecked() : LiveData<Boolean> {
        return overrideExpirationValue
    }

    fun setExpireOverrideChecked(isChecked : Boolean) {
        if (isChecked != overrideExpirationValue.value) {
            overrideExpirationValue.value = isChecked
        }
    }

    fun setTimerDisclaimerAcknowledged(acknowledged : Boolean) {
        appRepository.setAcknowledgedEarlyTimerDisclaimer(acknowledged)
        timerDisclaimerAcknowledged.value = acknowledged
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
            testSessionResult.postValue(testSessionResult.value)
        }
    }

    fun getRawImageCapturePath() : LiveData<String> {
        return rawImageCapturePath
    }

    fun setCapturedImage(imageData: Pair<String, MutableMap<String, String>>) {
        if (rawImageCapturePath.value != imageData.first) {
            recordCaptureAttempt()
            rawImageCapturePath.value = imageData.first

            val result = testSessionResult.value!!
            result.timeRead = Date()
            result.mainImage = imageData.first
            result.images.clear()
            result.images.putAll(imageData.second)

            result.results.clear()
            result.classifierResults.clear()
            workChecked.value = false
            processingErrorValue.value = null
            testSessionResult.value = result

            if (classifierMode != ClassifierMode.NONE) {
                processingStateValue.value = ProcessingState.PROCESSING
            } else {
                processingStateValue.value = ProcessingState.COMPLETE
            }
        }
    }


    fun setSecondaryImageCaptured(imageData: Pair<String, MutableMap<String, String>>) {
        if (secondaryImageCapturePath.value != imageData.first) {
            this.secondaryImageCaptured.value = true
            this.secondaryImageCapturePath.value = imageData.first
            testSessionResult.value!!.images.putAll(imageData.second.mapKeys { "secondary_" + it.key })
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
        //Disabled for now until cleared
        if (false && classifierMode == ClassifierMode.PRE_POPULATE) {
            for (e in classifierResults) {
                setResultValue(e.key, e.value)
            }
        }
        processingStateValue.postValue(ProcessingState.COMPLETE)
    }

    fun setProcessingError(error: String, details: Pamphlet?) {
        processingStateValue.postValue(ProcessingState.ERROR)
        this.processingErrorValue.postValue(Pair(error, details))
        this.numberOfFailedCaptures.postValue(numberOfFailedCaptures.value!! + 1)
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!sessionRepository.exists(sessionId)) {
                sessionIsInvalid.postValue(true)
            } else {
                val session = sessionRepository.getTestSession(sessionId)

                testSession.postValue(session)

                if (session.result == null && session.state == STATUS.RUNNING) {
                    Log.d(TAG, "Creating new placeholder result")
                    session.result = TestSession.TestResult(null, null, HashMap(), HashMap(), HashMap())
                }

                testSessionResult.postValue(session.result)

                updateTestState(session.getTestReadableState())

                classifierMode = session.configuration.classifierMode
                if (session.state == STATUS.RUNNING) {
                    startTimersForState(session, diagnosticsRepository.getTestProfile(session.testProfileId))
                }
            }
        }
    }

    private fun startTimersForState(session : TestSession, profile : RdtDiagnosticProfile) {
        viewModelScope.launch(Dispatchers.Main) {
            if (session.getTestReadableState() == TestReadableState.RESOLVING) {
                resolveTimer = object : CountDownTimer(session.timeResolved.time - System.currentTimeMillis(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        resolveMillisecondsLeft.postValue(millisUntilFinished)
                        if (session.timeExpired != null) {
                           readableMillisecondsLeft.postValue(session.timeExpired!!.time- System.currentTimeMillis())
                        }
                    }

                    override fun onFinish() {
                        // There can be some slight overlap on these so wait an extra hair before
                        // moving on
                        Thread.sleep(200L)
                        updateTestState(session.getTestReadableState())
                        startTimersForState(session, profile)
                    }
                }.start()
            } else if (session.getTestReadableState() == TestReadableState.READABLE) {
                if(session.timeExpired != null) {
                    readableTimer = object : CountDownTimer(session.timeExpired!!.time - System.currentTimeMillis(), 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            readableMillisecondsLeft.postValue(millisUntilFinished)
                        }

                        override fun onFinish() {
                            // There can be some slight overlap on these so wait an extra hair before
                            // moving on
                            Thread.sleep(200L)
                            updateTestState(session.getTestReadableState())
                        }
                    }.start()
                }
            }
        }
    }

    private fun updateTestState(newState : TestReadableState) {
        //Lots of behavior is triggered by these transitions, so only trigger them if there's
        //a real change
        if(testState.value != newState) {
            testState.postValue(newState)
        }
    }

    fun forceTestReadable() {
        //TODO: Flag "force" actions somewhere in the session log
        updateTestState(TestReadableState.READABLE)
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

    fun setProcessingSkipped() {
        this.processingStateValue.value = ProcessingState.SKIPPED
    }

    init {
        testState.value = TestReadableState.LOADING
        timerDisclaimerAcknowledged.value = appRepository.hasAcknowledgedEarlyTimerDisclaimer()
    }
    companion object {
        const val TAG = "CaptureViewModel"
    }
}

enum class ProcessingState {
    PRE_CAPTURE,
    PROCESSING,
    SKIPPED,
    COMPLETE,
    ERROR
}