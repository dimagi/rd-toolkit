package org.rdtoolkit.ui.capture

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import org.rdtoolkit.R
import org.rdtoolkit.model.diagnostics.DiagnosticOutcome
import org.rdtoolkit.model.diagnostics.ResultProfile

class ResultEntryAdapter(private val resultProfiles: Array<ResultProfile>) :
        RecyclerView.Adapter<ResultEntryAdapter.MyViewHolder>() {

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

        holder.view.tag = profile.id()

        resultName.text = String.format(context.getString(R.string.adapter_result_name_msg),
                profile.readableName());

        resultRadioGroup.removeAllViews();

        for (outcome  in profile.outcomes()) {
            //TODO: Inflate rather than code
            val button = RadioButton(context)
            button.text = outcome.readableName()
            button.tag = outcome.id()
            resultRadioGroup.addView(button)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = resultProfiles.size
}