package org.rdtoolkit.ui.sessions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.rdtoolkit.R;
import org.rdtoolkit.model.session.TestSession;
import org.rdtoolkit.ui.capture.CaptureViewModel;
import org.rdtoolkit.util.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

public class SessionsFragment extends Fragment {

    TestSessionsAdapter adapter;

    private SessionsViewModel sessionsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sessionsViewModel =
                new ViewModelProvider(this,
                        InjectorUtils.Companion.provideSessionsViewModelFactory(requireContext()))
                        .get(SessionsViewModel.class);

        View root = inflater.inflate(R.layout.fragment_sessions, container, false);

        adapter = new TestSessionsAdapter(sessionsViewModel);
        ((RecyclerView)root.findViewById(R.id.sessions_card_list)).setAdapter(adapter);
        ((RecyclerView)root.findViewById(R.id.sessions_card_list)).setLayoutManager(new LinearLayoutManager(requireContext()));

        sessionsViewModel.getTestSessions().observe(getViewLifecycleOwner(), result -> {
            adapter.submitList(result);
        });

        return root;
    }
}