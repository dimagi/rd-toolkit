package org.rdtoolkit.ui.capture;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.vrgsoft.arcprogress.ArcProgressBar;

import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.ResultProfile;
import org.rdtoolkit.model.session.TestReadableState;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.rdtoolkit.util.UtilsKt.getFormattedTimeForSpan;

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
            updateTestImagePreview(
                    (ImageView)view.findViewById(R.id.capture_results_test_image), value);
        });
    }

    private void updateTestImagePreview(ImageView imageView, String currentPhotoPath) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();

        if (targetW == 0) {
            targetW = imageView.getLayoutParams().width;
        }

        if(targetW == 0) { return;}

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, photoW/targetW);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

}