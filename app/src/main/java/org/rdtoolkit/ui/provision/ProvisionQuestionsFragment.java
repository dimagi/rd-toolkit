package org.rdtoolkit.ui.provision;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.rdtoolkit.BuildConfig;
import org.rdtoolkit.R;
import org.rdtoolkit.model.diagnostics.RdtDiagnosticProfile;
import org.rdtoolkit.util.Choice;
import org.rdtoolkit.util.ContextUtils;

import java.util.ArrayList;

import static org.rdtoolkit.support.model.session.SessionFlagsKt.FLAG_SESSION_TESTING_QA;
import static org.rdtoolkit.support.model.session.SessionFlagsKt.FLAG_VALUE_SET;
import static org.rdtoolkit.support.model.session.SessionFlagsKt.checkCaptureFlag;

public class ProvisionQuestionsFragment extends Fragment {

    static final String CHOICE_YES = "yes";
    static final String CHOICE_NO = "no";
    static final String CHOICE_BLANK = "blank";

    private ProvisionViewModel mViewModel;

    public static ProvisionQuestionsFragment newInstance() {
        return new ProvisionQuestionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provision_questions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ProvisionViewModel.class);

        Spinner spinner = view.findViewById(R.id.provision_question_spinner);
        spinner.setVisibility(View.VISIBLE);

        mViewModel.getCurrentRequiredInput().observe(getViewLifecycleOwner(), flag -> {

            boolean valueSet = checkCaptureFlag(mViewModel.getSessionConfig().getValue(), flag);
            boolean valueProvided = mViewModel.getInputsProvided().getValue().contains(flag);

            ArrayList<Choice> yesNoChoices = new ArrayList<>();

            Choice blank = new Choice(this.getContext(), CHOICE_BLANK, R.string.choice_blank);
            if(!valueProvided) {
                yesNoChoices.add(blank);
            }

            Choice yes = new Choice(this.getContext(),CHOICE_YES, R.string.choice_yes);
            yesNoChoices.add(yes);
            Choice no = new Choice(this.getContext(),CHOICE_NO, R.string.choice_no);
            yesNoChoices.add(no);

            ArrayAdapter adapter = new ArrayAdapter<>(this.getContext(),
                    R.layout.component_spinner_text, yesNoChoices);

            spinner.setAdapter(adapter);
            int defaultChoice = -1;

            if (!valueProvided) {
                defaultChoice = yesNoChoices.indexOf(blank);
            } else if(valueSet) {
                defaultChoice = yesNoChoices.indexOf(yes);
            } else {
                defaultChoice = yesNoChoices.indexOf(no);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Choice selected = yesNoChoices.get(i);
                    String id = selected.getId();
                    if(CHOICE_BLANK.equals(id)) {
                        return;
                    } else {
                        if(adapter.getPosition(blank) != -1) {
                            yesNoChoices.remove(blank);
                            adapter.notifyDataSetChanged();
                            spinner.setSelection(adapter.getPosition(selected));
                        }
                    }

                    if(CHOICE_YES.equals(id)) {
                        mViewModel.setFlagProvided(flag);
                    } else if(CHOICE_NO.equals(id)) {
                        mViewModel.setFlagUnavailable(flag);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spinner.setSelection(defaultChoice);
        });
    }
}