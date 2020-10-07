package org.rdtoolkit.ui.capture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.ResultProfile;

import static org.rdtoolkit.util.MediaUtilKt.setImageBitmapFromFile;

public class CaptureResultsFragment extends Fragment {

    private CaptureViewModel mViewModel;
    private RecyclerView resultEntryRecyclerView;
    private RecyclerView.Adapter entryAdapter;
    private RecyclerView.LayoutManager entryLayoutManager;


    public static CaptureResultsFragment newInstance() {
        return new CaptureResultsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resultEntryRecyclerView = view.findViewById(R.id.capture_results_entry_list);
        // use a linear layout manager
        entryLayoutManager = new LinearLayoutManager(requireContext());
        resultEntryRecyclerView.setLayoutManager(entryLayoutManager);

        mViewModel = new ViewModelProvider(requireActivity()).get(CaptureViewModel.class);

        mViewModel.getTestProfile().observe(getViewLifecycleOwner(), value -> {
            // specify an adapter (see also next example)
            entryAdapter = new ResultEntryAdapter(value.resultProfiles().toArray(new ResultProfile[0]), mViewModel);
            resultEntryRecyclerView.setAdapter(entryAdapter);
        });

        mViewModel.getRawImageCapturePath().observe(getViewLifecycleOwner(), value ->{
            setImageBitmapFromFile(
                    (ImageView)view.findViewById(R.id.capture_results_test_image), value);
        });
    }
}