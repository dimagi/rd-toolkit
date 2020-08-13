package org.rdtoolkit.ui.capture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.rdtoolkit.R;
import org.rdtoolkit.model.session.TestReadableState;
import org.rdtoolkit.ui.provision.ProvisionViewModel;

import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CaptureTimerFragment extends Fragment {

    private CaptureViewModel mViewModel;

    public static CaptureTimerFragment newInstance() {
        return new CaptureTimerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CaptureViewModel.class);

        mViewModel.getTestProfile().observe(getViewLifecycleOwner(), value -> {
            ((ProgressBar)view.findViewById(R.id.capture_resolve_countdown)).setMax(value.timeToResolve()*1000);
        });

        mViewModel.getMillisUntilResolved().observe(getViewLifecycleOwner(), value ->{
            ((ProgressBar)view.findViewById(R.id.capture_resolve_countdown)).setProgress(value.intValue());
            ((TextView)view.findViewById(R.id.capture_text_resolve_time)).setText(String.format("%d", value.intValue()));
        });

        mViewModel.getTestState().observe(getViewLifecycleOwner(), value -> {
            view.findViewById(R.id.capture_frame_time_resolving).setVisibility(
                    value == TestReadableState.RESOLVING ? VISIBLE : GONE);

            view.findViewById(R.id.capture_btn_result).setVisibility(
                    value == TestReadableState.READABLE ? VISIBLE : GONE);

            String status = "";

            switch(value) {
                case LOADING:
                    status = "Loading test details...";
                    break;
                case PREPARING:
                    //Note: Shouldn't be able to get here, really
                    status = "Test is not yet prepared";
                    break;
                case RESOLVING:
                    status = "Test is resolving, please wait for timer";
                    break;
                case READABLE:
                    status = "Test is available to read";
                    break;
                case EXPIRED:
                    status = "Test result has expired, and cannot be read";
                    break;
            }
            ((TextView)view.findViewById(R.id.capture_text_status)).setText(status);
        });
    }
}