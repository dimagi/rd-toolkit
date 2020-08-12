package org.rdtoolkit.ui.instruct;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SessionInstructViewModel extends ViewModel {
    private MutableLiveData<Boolean> viewInstructions;

    public SessionInstructViewModel() {
        viewInstructions = new MutableLiveData<Boolean>();
        viewInstructions.setValue(true);
    }
}