package org.rdtoolkit.ui.capture

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_capture_secondary.view.*
import org.rdtoolkit.R
import org.rdtoolkit.model.diagnostics.*

class ResultEntryAdapter(resultProfilesInput: Array<ResultProfile>,
private val viewModel: CaptureViewModel, private val allowIndeterminate : Boolean
) :
RecyclerView.Adapter<ResultEntryAdapter.MyViewHolder>() {

    private val resultProfiles : List<ResultProfile>
    private val inControlMode : Boolean

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ResultEntryAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_result_entry, parent, false) as View
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val context = holder.view.context;
        val resultName : TextView = holder.view.findViewById(R.id.adapter_result_name)
        val resultRadioGroup : RadioGroup = holder.view.findViewById(R.id.adapter_result_group)

        val profile : ResultProfile = resultProfiles[position]

        resultRadioGroup.tag = profile.id()

        resultName.text = String.format(context.getString(R.string.adapter_result_name_msg),
                profile.readableName());

        resultRadioGroup.removeAllViews();

        for (outcome  in profile.outcomes()) {
            //TODO: Inflate rather than code
            val button = RadioButton(context)
            button.text = outcome.readableName()
            button.tag = outcome.id()
            resultRadioGroup.addView(button)

            if (outcome.id() ==  RESULT_INDETERMINATE && !allowIndeterminate) {
                button.visibility = View.GONE
            }

            if(inControlMode && profile.id() != UNIVERSAL_CONTROL_FAILURE) {
                button.isEnabled = false
                viewModel.getUserEnteredControlStatus().observe(context as LifecycleOwner, Observer { value ->
                    button.isEnabled = value == CONTROL_VALID
                })

                //In aggregated control mode, hide the individual buttons
                if(outcome.id() == UNIVERSAL_CONTROL_FAILURE) {
                    button.visibility = View.GONE
                }
            }

            if(profile.id() == UNIVERSAL_CONTROL_FAILURE) {
                button.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(button: CompoundButton?, checked: Boolean) {
                        if (checked) {
                            val status = button!!.tag as String
                            viewModel.setUserEnteredControlStatus(status)
                        }
                    }
                })

                viewModel.getUserEnteredControlStatus().observe(context as LifecycleOwner, Observer { value ->
                    if (outcome.id() == value) {
                        button.isChecked = true
                    }
                })

            } else {
                button.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                    override fun onCheckedChanged(button: CompoundButton?, checked: Boolean) {
                        if (checked) {
                            val group: RadioGroup = button!!.parent as RadioGroup
                            val outcomeId = group.tag as String
                            val diagnosisId = button!!.tag as String

                            viewModel.setResultValue(outcomeId, diagnosisId)
                        }
                    }
                })
                viewModel.getTestSessionResult().observe(context as LifecycleOwner, Observer { value ->
                    if (outcome.id() == value.results.get(profile.id())) {
                        button.isChecked = true
                    }
                })
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = resultProfiles.size

    init {
        if (resultProfilesInput.fold(false){s, p -> s || p.outcomes().fold(s){s2, o -> s2 || o.id() == UNIVERSAL_CONTROL_FAILURE} }) {
            inControlMode = true
            resultProfiles = listOf(ControlFailureProfile()) + resultProfilesInput
        } else {
            inControlMode = false
            resultProfiles = resultProfilesInput.toList()
        }

    }
}

class ControlFailureProfile : ResultProfile {
    override fun id(): String {
        return UNIVERSAL_CONTROL_FAILURE;
    }

    override fun readableName(): String {
        return "Control Line"
    }

    override fun outcomes(): Collection<DiagnosticOutcome> {
        return listOf(ConcreteDiagnosticOutcome(UNIVERSAL_CONTROL_FAILURE, "Not present - Invalid test"),
                ConcreteDiagnosticOutcome(CONTROL_VALID, "Present")
        )
    }
}