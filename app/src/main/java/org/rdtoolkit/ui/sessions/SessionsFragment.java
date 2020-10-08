package org.rdtoolkit.ui.sessions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.rdtoolkit.R;
import org.rdtoolkit.util.InjectorUtils;

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