package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import io.primer.android.R
import io.primer.android.ui.FormProgressState

internal class FormProgressFragment : FormChildFragment() {
  private lateinit var mLayout: ViewGroup

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return inflater.inflate(R.layout.fragment_form_progress, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    mLayout = view.findViewById(R.id.fragment_form_progress_container)

    viewModel.progress.observe(viewLifecycleOwner) {
      if (it == null) {
        mLayout.visibility = View.INVISIBLE
      } else {
        mLayout.visibility = View.VISIBLE
        addIndicators(it)
      }
    }
  }

  private fun addIndicators(state: FormProgressState) {
    mLayout.removeAllViews()

    for (i in 1..state.max) {
      mLayout.addView(createIndicator(i == state.current))
    }

    mLayout.requestLayout()
  }

  private fun createIndicator(active: Boolean): View {
    val view = View(context)

    val scale = requireContext().resources.displayMetrics.density
    val width = (8 * scale + 0.5f)
    val margin = (4 * scale + 0.5f)

    view.background = ContextCompat.getDrawable(requireContext(), R.drawable.progress_crumb)
    view.isEnabled = active
    view.layoutParams = LinearLayout.LayoutParams(width.toInt(), width.toInt(), 0.0f).apply {
      marginStart = margin.toInt()
      marginEnd = margin.toInt()
    }

    return view
  }
}