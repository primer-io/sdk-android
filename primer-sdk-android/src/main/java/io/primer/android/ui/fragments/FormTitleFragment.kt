package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import io.primer.android.R
import io.primer.android.databinding.FragmentFormTitleBinding
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormTitleState
import io.primer.android.ui.extensions.autoCleaned

internal class FormTitleFragment : FormChildFragment() {

    private var binding: FragmentFormTitleBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFormTitleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.formTitleGoBack.setOnClickListener {
            dispatchFormEvent(FormActionEvent.GoBack())
        }

        binding.formTitleCancelButton.setOnClickListener {
            dispatchFormEvent(
                if (viewModel.title.value?.cancelBehaviour == FormTitleState.CancelBehaviour.CANCEL)
                    FormActionEvent.Cancel()
                else
                    FormActionEvent.Exit()
            )
        }

        viewModel.title.observe(viewLifecycleOwner) {
            val titleView = binding.formTitle
            val descriptionView = binding.formTitleDescription

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
        binding.formProgressContainer.removeAllViews()

        if (state == null) {
            binding.formProgressContainer.visibility = View.GONE
        } else {
            binding.formProgressContainer.visibility = View.VISIBLE

            for (i in 1..state.max) {
                binding.formProgressContainer.addView(createIndicator(i == state.current))
            }
        }

        binding.formProgressContainer.requestLayout()
    }

    private fun createIndicator(active: Boolean): View {
        val view = View(context)

        val scale = requireContext().resources.displayMetrics.density
        val width = (SCALE_8 * scale + HALF)
        val margin = (SCALE_4 * scale + HALF)

        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.progress_crumb)
        view.isEnabled = active
        view.layoutParams = LinearLayout.LayoutParams(width.toInt(), width.toInt(), 0.0f).apply {
            marginStart = margin.toInt()
            marginEnd = margin.toInt()
        }

        return view
    }
}

private const val SCALE_4 = 4
private const val SCALE_8 = 8
private const val HALF = 0.5f
