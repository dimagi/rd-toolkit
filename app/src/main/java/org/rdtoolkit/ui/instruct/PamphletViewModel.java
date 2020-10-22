package org.rdtoolkit.ui.instruct;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.rdtoolkit.model.diagnostics.Page;
import org.rdtoolkit.model.diagnostics.Pamphlet;

import java.util.ArrayList;
import java.util.List;

public class PamphletViewModel extends ViewModel {
    public static final int PAGE_NONE = -2;

    private Page disclaimerPage;

    public PamphletViewModel(DisclaimerPage disclaimerPage) {
        this.disclaimerPage = disclaimerPage;
    }

    private MutableLiveData<List<Page>> pageList = new MutableLiveData<>();

    private int pageNumber = PAGE_NONE;

    private Pamphlet sourcePamphlet;

    private MutableLiveData<Page> currentPage = new MutableLiveData();

    public void setSourcePamphlet(Pamphlet pamphlet) {
        this.sourcePamphlet = pamphlet;
        pageNumber = PAGE_NONE;

        List<Page> pages = new ArrayList();
        if(disclaimerPage != null) {
            pages.add(disclaimerPage);
        }
        pages.addAll(sourcePamphlet.getPages());
        pageList.setValue(pages);
        goToPageOne();
    }

    public LiveData<List<Page>> getPages() {
        return pageList;
    }

    public void goToPageOne() {
        pageNumber = 0;
        currentPage.setValue(pageList.getValue().get(0));
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
