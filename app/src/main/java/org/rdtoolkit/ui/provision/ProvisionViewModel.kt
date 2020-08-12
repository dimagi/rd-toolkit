package org.rdtoolkit.ui.provision

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.rdtoolkit.model.session.STATUS
import org.rdtoolkit.model.session.SessionRepository
import org.rdtoolkit.model.session.TestSession
import java.util.*

const val TAG = "ProvisionViewModel"

class ProvisionViewModel(repository: SessionRepository) : ViewModel() {
    private val viewInstructions: MutableLiveData<Boolean>
    val sessionId: String
    private val repository: SessionRepository

    fun getViewInstructions(): LiveData<Boolean> {
        return viewInstructions
    }

    fun setViewInstructions(setValue: Boolean) {
        if (viewInstructions.value != setValue) {
            viewInstructions.value = setValue
        }
    }

    fun commitSession() : String{
        val session = TestSession(sessionId, STATUS.RUNNING, "Clayton Sims", "#423423",
                Date(Date().time + 1000 * 10 * 2),
                Date(Date().time + 1000 * 10 * 4))

        val job = viewModelScope.launch(Dispatchers.IO) {
            repository.insert(session)
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
        this.repository = repository
    }
}