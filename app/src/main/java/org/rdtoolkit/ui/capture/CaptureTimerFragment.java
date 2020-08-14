package org.rdtoolkit.ui.capture;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.vrgsoft.arcprogress.ArcProgressBar;

import org.rdtoolkit.R;
import org.rdtoolkit.model.session.TestReadableState;
import org.rdtoolkit.ui.provision.ProvisionViewModel;

import java.util.Date;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.View.resolveSize;
import static org.rdtoolkit.util.UtilsKt.getFormattedTimeForSpan;

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
            ((ArcProgressBar)view.findViewById(R.id.capture_resolve_countdown)).setMax(value.timeToResolve()*1000);
            ((ProgressBar)view.findViewById(R.id.capture_timer_expiring_progress)).setMax((value.timeToExpire() - value.timeToResolve())*1000);
        });

        mViewModel.getMillisUntilResolved().observe(getViewLifecycleOwner(), value ->{
            String formattedTime = getFormattedTimeForSpan(value);
            ((ArcProgressBar)view.findViewById(R.id.capture_resolve_countdown)).setProgress(value.intValue());
            ((ArcProgressBar)view.findViewById(R.id.capture_resolve_countdown)).setProgressText(formattedTime);
        });

        mViewModel.getMillisUntilExpired().observe(getViewLifecycleOwner(), value ->{
            String formattedTime = getFormattedTimeForSpan(value);
            ((ProgressBar)view.findViewById(R.id.capture_timer_expiring_progress)).setProgress(value.intValue());
            ((TextView)view.findViewById(R.id.capture_timer_expiring_countdown)).setText(
                    String.format(getString(R.string.capture_timer_expiring_countdown_msg),
                            formattedTime));
        });

        mViewModel.getTestState().observe(getViewLifecycleOwner(), value -> {
            View resolvingFrame = view.findViewById(R.id.capture_frame_time_resolving);
            if (value == TestReadableState.RESOLVING) {
                resolvingFrame.setVisibility(VISIBLE);
            } else if(value == TestReadableState.READABLE && resolvingFrame.getVisibility() == VISIBLE) {
                AnimatorSet animSet =
                        (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.card_flip_right_out);
                animSet.setTarget(resolvingFrame);
                animSet.start();
                animSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        resolvingFrame.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            } else {
                resolvingFrame.setVisibility(View.INVISIBLE);
            }

            View testResolvedFrame = view.findViewById(R.id.capture_frame_resolved);
            if (value == TestReadableState.READABLE) {
                testResolvedFrame.setVisibility(VISIBLE);
                AnimatorSet animSet =
                        (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.card_flip_right_in);
                animSet.setTarget(testResolvedFrame);
                animSet.start();

                resolvingFrame.setVisibility(VISIBLE);
            } else {
                testResolvedFrame.setVisibility(GONE);
            }

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