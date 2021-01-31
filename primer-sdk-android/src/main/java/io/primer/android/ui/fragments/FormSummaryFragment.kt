package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.FormSummaryState
import io.primer.android.ui.InteractiveSummaryItem
import io.primer.android.ui.TextSummaryItem

internal class FormSummaryFragment : FormChildFragment() {
  private lateinit var layout: ViewGroup
  private val log = Logger("form-summary-fragment")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_summary, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    layout = view.findViewById(R.id.form_summary_fragment)

    viewModel.summary.observe(viewLifecycleOwner) {
      if (it == null) {
        hideView()
      } else {
        showView(it)
      }
    }
  }

  private fun hideView() {
    layout.removeAllViews()
    layout.visibility = View.GONE
  }

  private fun showView(state: FormSummaryState) {
    layout.removeAllViews()
    layout.visibility = View.VISIBLE

    state.items.forEach {
      layout.addView(createItem(it))
    }

    if (state.items.isNotEmpty() && state.text.isNotEmpty()) {
      layout.addView(createSpacer())
    }

    state.text.forEachIndexed { index, content ->
      layout.addView(createTextView(content, isFirst = index == 0))
    }
  }

  private fun createItem(item: InteractiveSummaryItem): View {
    val view = View.inflate(requireContext(), R.layout.form_summary_item, null)

    view.findViewById<ImageView>(R.id.form_summary_item_icon).setImageDrawable(
      ResourcesCompat.getDrawable(requireActivity().resources, item.iconId, null)
    )

    view.findViewById<TextView>(R.id.form_summary_item_text).setText(item.label)

    view.setOnClickListener {
      dispatchFormEvent(FormActionEvent.SummaryItemPress(item.name))
    }

    return view
  }

  private fun createSpacer(): View {
    val spacer = Space(requireContext())
    spacer.minimumHeight = 12
    return spacer
  }

  private fun createTextView(item: TextSummaryItem, isFirst: Boolean = false): TextView {
    val view = TextView(requireContext())
    view.id = View.generateViewId()
    view.text = item.content
    TextViewCompat.setTextAppearance(view, item.styleId)

    if (!isFirst) {
      val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      )
      params.topMargin = 12
      view.layoutParams = params
    }

    return view
  }
}