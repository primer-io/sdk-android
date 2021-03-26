package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormTitleState

internal class FormTitleFragment : FormChildFragment() {

    private val log = Logger("form-title-fragment")
    private lateinit var mProgress: ViewGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_form_title, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProgress = view.findViewById(R.id.form_progress_container)

        view.findViewById<ImageView>(R.id.form_title_go_back).setOnClickListener {
            dispatchFormEvent(FormActionEvent.GoBack())
        }

        view.findViewById<Button>(R.id.form_title_cancel_button).setOnClickListener {
            dispatchFormEvent(
                if (viewModel.title.value?.cancelBehaviour == FormTitleState.CancelBehaviour.CANCEL) FormActionEvent.Cancel() else FormActionEvent.Exit()
            )
        }

        viewModel.title.observe(viewLifecycleOwner) {
            val titleView = view.findViewById<TextView>(R.id.form_title)
            val descriptionView = view.findViewById<TextView>(R.id.form_title_description)

            it?.let { state ->
                titleView.text = requireContext().getString(state.titleId)

                if (state.descriptionId == null) {
                    descriptionView.visibility = View.GONE
                } else {
                    descriptionView.visibility = View.VISIBLE
                    descriptionView.text = requireContext().getString(state.descriptionId)
                }
            }
        }
        viewModel.progress.observe(viewLifecycleOwner) {
            addIndicators(it)
        }
    }

    private fun addIndicators(state: FormProgressState?) {
        mProgress.removeAllViews()

        if (state == null) {
            mProgress.visibility = View.GONE
        } else {
            mProgress.visibility = View.VISIBLE

            for (i in 1..state.max) {
                mProgress.addView(createIndicator(i == state.current))
            }
        }

        mProgress.requestLayout()
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
