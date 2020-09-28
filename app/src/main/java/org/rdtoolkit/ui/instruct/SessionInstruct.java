package org.rdtoolkit.ui.instruct;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.rdtoolkit.R;
import org.rdtoolkit.ui.provision.ProvisionViewModel;

public class SessionInstruct extends Fragment {

    private SessionInstructViewModel mViewModel;

    public static SessionInstruct newInstance() {
        return new SessionInstruct();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provision_instruct, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView iv = view.findViewById(R.id.provision_instructions);
        
    }
}