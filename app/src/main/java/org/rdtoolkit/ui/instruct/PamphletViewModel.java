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

    private List<Page> pageList;

    private int pageNumber = PAGE_NONE;

    private Pamphlet sourcePamphlet;

    private MutableLiveData<Page> currentPage = new MutableLiveData();
    //private MutableLiveData<Boolean> checked = new MutableLiveData();

    public void setSourcePamphlet(Pamphlet pamphlet) {
        this.sourcePamphlet = pamphlet;
        pageNumber = PAGE_NONE;

        pageList = new ArrayList();
        if(disclaimerPage != null) {
            pageList.add(disclaimerPage);
        }
        pageList.addAll(sourcePamphlet.getPages());
        goToPageOne();
    }

    public void goToPageOne() {
        pageNumber = 0;
        currentPage.setValue(pageList.get(0));
    }

    public LiveData<Page> getCurrentPage() {
        return currentPage;
    }

    public boolean hasBack() {
        return pageNumber > 0;
    }

    public boolean hasNext() {
        return pageNumber < (pageList.size() - 1);
    }

    public void pageBack() {
        if(hasBack()) {
            pageNumber--;
            currentPage.setValue(pageList.get(pageNumber));
        }
    }

    public void pageNext() {
        if(hasNext()) {
            pageNumber++;
            currentPage.setValue(pageList.get(pageNumber));
        }
    }
}