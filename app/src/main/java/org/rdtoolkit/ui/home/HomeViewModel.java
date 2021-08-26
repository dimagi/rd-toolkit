package org.rdtoolkit.ui.home;

import androidx.lifecycle.ViewModel;

import org.rdtoolkit.model.session.AppRepository;

public class HomeViewModel extends ViewModel {

    AppRepository appRepository;

    public HomeViewModel(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public AppRepository getAppRepository() {
        return appRepository;
    }
}