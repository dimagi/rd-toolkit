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
import org.rdtoolkit.R
import org.rdtoolkit.model.diagnostics.ResultProfile

class ResultSummaryAdapter(private val resultProfiles: List<ResultProfile>,
                           private val results: Map<String, String>
) :
RecyclerView.Adapter<ResultSummaryAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val view : View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ResultSummaryAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.component_result_row, parent, false) as View
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val context = holder.view.context;
        val caption : TextView = holder.view.findViewById(R.id.summary_row_caption)
        val value : TextView = holder.view.findViewById(R.id.summary_row_value)

        val profile : ResultProfile = resultProfiles[position]

        caption.text = profile.readableName()
        val resultType = results[profile.id()]
        resultType?.let{
            value.text = profile.outcomes().filter{it.id() == resultType}.first().readableName()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = resultProfiles.size
}