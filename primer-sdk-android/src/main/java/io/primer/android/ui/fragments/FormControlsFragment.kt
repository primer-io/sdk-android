package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.ButtonState

internal class FormControlsFragment : FormChildFragment() {
  private val log = Logger("form-controls")
  private lateinit var layout: ViewGroup

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_controls, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    layout = view.findViewById(R.id.form_button)

    viewModel.button.observe(viewLifecycleOwner) {
      it?.let { state ->
        val buttonText = layout.findViewById<TextView>(R.id.form_button_txt)
        val buttonLoading = layout.findViewById<ProgressBar>(R.id.form_button_loading)

        layout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
          gravity = when(state.placement) {
            ButtonState.Placement.LEFT -> Gravity.START
            ButtonState.Placement.RIGHT -> Gravity.END
            else -> Gravity.CENTER_HORIZONTAL
          }
        }

        buttonText.text = requireContext().getString(state.labelId)

        buttonLoading.visibility = if (state.loading) View.VISIBLE else View.GONE
        layout.isEnabled = isViewEnabled(viewModel.isValid.value ?: true, it)
      }
    }

    viewModel.isValid.observe(viewLifecycleOwner) {
      view.isEnabled = isViewEnabled(it, viewModel.button.value)
    }

    view.setOnClickListener {
      dispatchFormEvent(FormActionEvent.SubmitPressed())
    }
  }

  private fun isViewEnabled(isValid: Boolean, button: ButtonState?): Boolean {
    return isValid && button?.loading == false
  }
}