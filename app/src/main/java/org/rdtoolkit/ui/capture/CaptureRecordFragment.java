package org.rdtoolkit.ui.capture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.rdtoolkit.R;
import org.rdtoolkit.model.session.TestReadableState;

import static org.rdtoolkit.model.session.SessionFlagsKt.FLAG_SESSION_NO_EXPIRATION_OVERRIDE;
import static org.rdtoolkit.model.session.SessionFlagsKt.FLAG_VALUE_SET;
import static org.rdtoolkit.model.session.SessionFlagsKt.FLAG_VALUE_UNSET;

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

        mViewModel.getTestState().observe(getViewLifecycleOwner(), result -> {
            int overrideVisibility =
                    FLAG_VALUE_SET.equals(mViewModel.getTestSession().getValue().getConfiguration().getFlags().get(FLAG_SESSION_NO_EXPIRATION_OVERRIDE)) ?
                    View.GONE :
                    View.VISIBLE;

            if (result == TestReadableState.EXPIRED) {
                view.findViewById(R.id.capture_frame_record_expired).setVisibility(View.VISIBLE);

                view.findViewById(R.id.capture_frame_record_expired_override).setVisibility(overrideVisibility);
                overrideButton.setVisibility(overrideVisibility);
            } else {
                view.findViewById(R.id.capture_frame_record_expired).setVisibility(View.GONE);
                overrideButton.setVisibility(View.GONE);
            }
        });
    }
}
