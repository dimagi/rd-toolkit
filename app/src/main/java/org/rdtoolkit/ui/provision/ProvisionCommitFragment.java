package org.rdtoolkit.ui.provision;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.rdtoolkit.BuildConfig;
import org.rdtoolkit.R;
import org.rdtoolkit.util.ContextUtils;

public class ProvisionCommitFragment extends Fragment {

    private ProvisionViewModel mViewModel;

    public static ProvisionCommitFragment newInstance() {
        return new ProvisionCommitFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provision_begin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ProvisionViewModel.class);
        mViewModel.getSelectedTestProfile().observe(getViewLifecycleOwner(), value -> {
            ((TextView)view.findViewById(R.id.provision_begin_resolve)).setText(
                    String.format(getString(R.string.provision_begin_resolve_msg),
                            new ContextUtils(requireContext()).
                                    getReadableTime(value.timeToResolve())));
        });

        CheckBox resolveImmediately = ((CheckBox)view.findViewById(R.id.provision_begin_cbx_resolve_immediately));

        if (BuildConfig.DEBUG) {
            resolveImmediately.setVisibility(View.VISIBLE);
        }

        resolveImmediately.setOnCheckedChangeListener((compoundButton, b) -> {
            mViewModel.setDebugResolveImmediately(b);
        });
        mViewModel.getDebugResolveImmediately().observe(getViewLifecycleOwner(), value -> {
            resolveImmediately.setChecked(value);
        });
    }
}