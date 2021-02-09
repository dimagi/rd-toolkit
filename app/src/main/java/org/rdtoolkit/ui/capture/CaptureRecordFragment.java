package org.rdtoolkit.ui.capture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.ResultProfile;
import org.rdtoolkit.support.model.session.TestReadableState;

import java.util.ArrayList;

import static org.rdtoolkit.support.model.session.SessionFlagsKt.FLAG_SESSION_NO_EXPIRATION_OVERRIDE;
import static org.rdtoolkit.support.model.session.SessionFlagsKt.FLAG_VALUE_SET;
import static org.rdtoolkit.util.MediaUtilKt.setImageBitmapFromFile;

public class CaptureRecordFragment extends Fragment {

    private CaptureViewModel mViewModel;

    public static CaptureResultsFragment newInstance() {
        return new CaptureResultsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView summaryRecycler = view.findViewById(R.id.capture_summary_results);
        LinearLayoutManager entryLayoutManager = new LinearLayoutManager(requireContext());
        summaryRecycler.setLayoutManager(entryLayoutManager);


        mViewModel = new ViewModelProvider(requireActivity()).get(CaptureViewModel.class);

        CheckBox allowOverride = view.findViewById(R.id.capture_frame_record_cbx_enable_override);

        Button overrideButton = view.findViewById(R.id.capture_btn_override_expiration);

        allowOverride.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mViewModel.setExpireOverrideChecked(b);
            }
        });

        mViewModel.getSessionCommit().observe(getViewLifecycleOwner(), result -> {
            view.findViewById(R.id.capture_record_btn_commit).setEnabled(!result.getFirst());
        });

        mViewModel.getExpireOverrideChecked().observe(getViewLifecycleOwner(), result -> {
            allowOverride.setChecked(result);
            overrideButton.setEnabled(result);
        });

        mViewModel.getTestCapturedLate().observe(getViewLifecycleOwner(), value -> {
            if(value) {
                view.findViewById(R.id.capture_summary_timer_valid_label).setVisibility(View.GONE);
                view.findViewById(R.id.capture_summary_timer_expired_label).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.capture_summary_timer_valid_label).setVisibility(View.VISIBLE);
                view.findViewById(R.id.capture_summary_timer_expired_label).setVisibility(View.GONE);
            }
        });

        mViewModel.getTestSessionResult().observe(getViewLifecycleOwner(), result -> {
            ArrayList<ResultProfile> values= new ArrayList();
            values.addAll(mViewModel.getTestProfile().getValue().resultProfiles());
            summaryRecycler.setAdapter(new ResultSummaryAdapter(values, result.getResults()));
        });

        mViewModel.getSessionStateInputs().observe(getViewLifecycleOwner(), result -> {
            int overrideVisibility =
                    FLAG_VALUE_SET.equals(mViewModel.getTestSession().getValue().getConfiguration().getFlags().get(FLAG_SESSION_NO_EXPIRATION_OVERRIDE)) ?
                    View.GONE :
                    View.VISIBLE;

            if (result.getFirst() == TestReadableState.EXPIRED && result.getSecond()) {
                view.findViewById(R.id.capture_frame_record_expired).setVisibility(View.VISIBLE);
                view.findViewById(R.id.capture_frame_record_summary).setVisibility(View.GONE);


                view.findViewById(R.id.capture_frame_record_expired_override).setVisibility(overrideVisibility);
                overrideButton.setVisibility(overrideVisibility);
            } else {
                view.findViewById(R.id.capture_frame_record_expired).setVisibility(View.GONE);
                view.findViewById(R.id.capture_frame_record_summary).setVisibility(View.VISIBLE);
                overrideButton.setVisibility(View.GONE);
            }
        });

        mViewModel.getRawImageCapturePath().observe(getViewLifecycleOwner(), value ->{
            setImageBitmapFromFile(
                    (ImageView)view.findViewById(R.id.capture_results_test_image), value);
        });

        mViewModel.getSecondaryImageCaptured().observe(getViewLifecycleOwner(), value -> {
            if (value) {
                view.findViewById(R.id.capture_results_secondary_frame).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.capture_results_secondary_frame).setVisibility(View.GONE);
            }
        });

        mViewModel.getSecondaryImageCapturePath().observe(getViewLifecycleOwner(), value ->{
            setImageBitmapFromFile(
                    (ImageView)view.findViewById(R.id.capture_results_secondary_test_image), value);
        });



    }
}
