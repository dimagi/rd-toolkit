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

public class CaptureCheckFragment extends Fragment {

    private CaptureViewModel mViewModel;

    public static CaptureResultsFragment newInstance() {
        return new CaptureResultsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture_check, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel = new ViewModelProvider(requireActivity()).get(CaptureViewModel.class);

        mViewModel.getRawImageCapturePath().observe(getViewLifecycleOwner(), value ->{
            setImageBitmapFromFile(
                    (ImageView)view.findViewById(R.id.capture_results_test_image), value);
        });
    }
}
