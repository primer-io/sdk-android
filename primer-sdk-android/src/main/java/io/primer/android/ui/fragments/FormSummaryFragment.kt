package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import io.primer.android.R
import io.primer.android.databinding.FragmentFormSummaryBinding
import io.primer.android.ui.FormSummaryState
import io.primer.android.ui.InteractiveSummaryItem
import io.primer.android.ui.TextSummaryItem
import io.primer.android.ui.extensions.autoCleaned

const val TOP_MARGIN_LARGE: Int = 64
const val TOP_MARGIN_SMALL: Int = 28

internal class FormSummaryFragment : FormChildFragment() {

    private var binding: FragmentFormSummaryBinding by autoCleaned()
    private val updaters: MutableList<() -> Unit> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFormSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.summary.observe(viewLifecycleOwner) {
            if (it == null) {
                hideView()
            } else {
                showView(it)
            }
        }
    }

    private fun hideView() {
        updaters.clear()
        binding.formSummaryFragment.removeAllViews()
        binding.formSummaryFragment.visibility = View.GONE
    }

    private fun showView(state: FormSummaryState) {
        updaters.clear()
        binding.formSummaryFragment.removeAllViews()
        binding.formSummaryFragment.visibility = View.VISIBLE

        state.items.forEach {
            binding.formSummaryFragment.addView(createItem(it))
        }

        updateAllViews()

        state.text.forEachIndexed { index, content ->
            binding.formSummaryFragment.addView(createTextView(content, isFirst = index == 0))
        }
    }

    override fun onResume() {
        super.onResume()
        updateAllViews()
    }

    private fun updateAllViews() {
        updaters.forEach { it() }
    }

    private fun createItem(item: InteractiveSummaryItem): View {
        val view = View.inflate(requireContext(), R.layout.form_summary_item, null)

        view.findViewById<ImageView>(R.id.form_summary_item_icon).setImageDrawable(
            ResourcesCompat.getDrawable(requireActivity().resources, item.iconId, null)
        )

        updaters.add {
            view.findViewById<TextView>(R.id.form_summary_item_text).text = item.getLabel()
        }

        view.setOnClickListener {
            dispatchFormEvent(FormActionEvent.SummaryItemPress(item.name))
        }

        return view
    }

    private fun createTextView(item: TextSummaryItem, isFirst: Boolean = false): TextView {
        val view = TextView(requireContext())
        view.id = View.generateViewId()
        view.text = item.content
        TextViewCompat.setTextAppearance(view, item.styleId)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.topMargin = if (isFirst) TOP_MARGIN_LARGE else TOP_MARGIN_SMALL

        view.layoutParams = params

        return view
    }
}
