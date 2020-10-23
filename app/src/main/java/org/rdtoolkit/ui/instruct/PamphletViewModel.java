package org.rdtoolkit.ui.instruct;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.rdtoolkit.model.diagnostics.Page;
import org.rdtoolkit.model.diagnostics.Pamphlet;
import org.rdtoolkit.model.session.AppRepository;

import java.util.ArrayList;
import java.util.List;

public class PamphletViewModel extends ViewModel {
    public static final int PAGE_NONE = -2;

    private Page disclaimerPage;
    private AppRepository appRepository;

    private MutableLiveData<Page> currentPage;
    private MutableLiveData<Boolean> disclaimerAcknowledged;

    public PamphletViewModel(DisclaimerPage disclaimerPage, AppRepository appRepository) {
        this.disclaimerPage = disclaimerPage;
        this.appRepository = appRepository;
        currentPage = new MutableLiveData();
        disclaimerAcknowledged = new MutableLiveData(appRepository.hasAcknowledgedDisclaimer());
    }

    private MutableLiveData<List<Page>> pageList = new MutableLiveData<>();

    private int pageNumber = PAGE_NONE;

    private Pamphlet sourcePamphlet;

    public LiveData<Boolean> isDisclaimerAcknowledged() {
        return disclaimerAcknowledged;
    }

    public void setDisclaimerAcknowledged(boolean acknowledged) {
        if(disclaimerAcknowledged.getValue() != acknowledged) {
            appRepository.setAcknowledgedDisclaimer(acknowledged);
            disclaimerAcknowledged.setValue(acknowledged);
        }
    }

    public void setSourcePamphlet(Pamphlet pamphlet) {
        this.sourcePamphlet = pamphlet;
        pageNumber = PAGE_NONE;

        List<Page> pages = new ArrayList();
        if(disclaimerPage != null) {
            pages.add(disclaimerPage);
        }
        pages.addAll(sourcePamphlet.getPages());
        pageList.setValue(pages);
        goToPageOne(true);
    }

    public LiveData<List<Page>> getPages() {
        return pageList;
    }

    public void goToPageOne() {
        goToPageOne(false);
    }

    public void goToPageOne(boolean force) {
        if(force || pageNumber != 0) {
            pageNumber = 0;
            currentPage.setValue(pageList.getValue().get(0));
        }
    }

    public LiveData<Page> getCurrentPage() {
        return currentPage;
    }

    public boolean hasBack() {
        return pageNumber > 0;
    }

    public boolean hasNext() {
        return pageNumber < (pageList.getValue().size() - 1);
    }

    public void pageBack() {
        if(hasBack()) {
            pageNumber--;
            currentPage.setValue(pageList.getValue().get(pageNumber));
        }
    }

    public void pageNext() {
        if(hasNext()) {
            pageNumber++;
            currentPage.setValue(pageList.getValue().get(pageNumber));
        }
    }
}
