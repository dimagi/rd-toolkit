package org.rdtoolkit.ui.capture;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.vrgsoft.arcprogress.ArcProgressBar;

import org.rdtoolkit.R;
import org.rdtoolkit.support.model.session.TestReadableState;

import kotlin.NotImplementedError;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.rdtoolkit.util.MediaUtilKt.setImageBitmapFromFile;
import static org.rdtoolkit.util.UtilsKt.getFormattedTimeForSpan;

public class CaptureSecondaryFragment extends Fragment {

    private CaptureViewModel mViewModel;

    public static CaptureSecondaryFragment newInstance() {
        return new CaptureSecondaryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture_secondary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(CaptureViewModel.class);

        mViewModel.getSecondaryRequestAcknowledged().observe(getViewLifecycleOwner(), result -> {
            if(result) {
                view.findViewById(R.id.capture_secondary_input_explanation).setVisibility(GONE);
                view.findViewById(R.id.capture_secondary_input_frame).setVisibility(VISIBLE);
            } else {
                view.findViewById(R.id.capture_secondary_input_explanation).setVisibility(VISIBLE);
                view.findViewById(R.id.capture_secondary_input_frame).setVisibility(GONE);
            }
        });

        mViewModel.getSecondaryImageCapturePath().observe(getViewLifecycleOwner(), result -> {
            view.findViewById(R.id.capture_secondary_results_frame).setVisibility(VISIBLE);
            setImageBitmapFromFile(
                    (ImageView)view.findViewById(R.id.capture_secondary_test_image), result);
        });
    }
}