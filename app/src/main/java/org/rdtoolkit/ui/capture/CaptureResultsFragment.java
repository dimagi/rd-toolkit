package org.rdtoolkit.ui.capture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile;
import org.rdtoolkit.model.diagnostics.ResultProfile;

import static org.rdtoolkit.support.model.session.SessionFlagsKt.FLAG_CAPTURE_ALLOW_INDETERMINATE;
import static org.rdtoolkit.support.model.session.SessionFlagsKt.isFlagSet;
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

        mViewModel.getTestSessionResult().observe(getViewLifecycleOwner(), value -> {
            RdtDiagnosticProfile profile = mViewModel.getTestProfile().getValue();
            boolean complete = false;
            if(profile != null && value != null) {
                complete = profile.isResultSetComplete(value);
            }
            if(complete) {
                view.findViewById(R.id.capture_results_proceed).setVisibility(View.VISIBLE);
                view.findViewById(R.id.capture_results_instruct).setVisibility(View.INVISIBLE);
            } else {
                view.findViewById(R.id.capture_results_proceed).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.capture_results_instruct).setVisibility(View.VISIBLE);
            }
        });

        mViewModel.getTestProfile().observe(getViewLifecycleOwner(), value -> {
            // specify an adapter (see also next example)
            entryAdapter = new ResultEntryAdapter(value.resultProfiles().toArray(new ResultProfile[0]), mViewModel, isFlagSet(mViewModel.getTestSession().getValue().getConfiguration(), FLAG_CAPTURE_ALLOW_INDETERMINATE, false));
            resultEntryRecyclerView.setAdapter(entryAdapter);
        });

        mViewModel.getRawImageCapturePath().observe(getViewLifecycleOwner(), value ->{
            setImageBitmapFromFile(
                    (ImageView)view.findViewById(R.id.capture_results_test_image), value);
        });
    }

}