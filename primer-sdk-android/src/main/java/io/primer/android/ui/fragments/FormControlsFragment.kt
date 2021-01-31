package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.ui.ButtonState

internal class FormControlsFragment : FormChildFragment() {
  private val log = Logger("form-controls")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_form_controls, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.button.observe(viewLifecycleOwner) {
      it?.let { state ->
        val buttonText = view.findViewById<TextView>(R.id.form_button_txt)
        val buttonLoading = view.findViewById<ProgressBar>(R.id.form_button_loading)

        buttonText.text = requireContext().getString(state.labelId)
        buttonLoading.visibility = if (state.loading) View.VISIBLE else View.GONE
        view.isEnabled = isViewEnabled(viewModel.isValid.value ?: true, it)
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