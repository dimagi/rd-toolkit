package org.rdtoolkit.ui.instruct;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SessionInstructViewModel extends ViewModel {
    public static final int PAGE_SPECIAL_DISCLAIMER = -1;
    public static final int PAGE_NONE = -2;


    private MutableLiveData<Integer> currentPage = new MutableLiveData<>(PAGE_NONE);



    public SessionInstructViewModel() {

    }
}