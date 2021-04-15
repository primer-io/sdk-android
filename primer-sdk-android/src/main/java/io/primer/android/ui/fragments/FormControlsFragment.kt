package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.components.ButtonPrimary
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class FormControlsFragment : FormChildFragment() {

    private lateinit var mButton: ButtonPrimary

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_form_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mButton = view.findViewById(R.id.form_button_cta)

        viewModel.button.observe(viewLifecycleOwner) {
            it?.let { state ->
                mButton.text = requireContext().getString(state.labelId)
                mButton.setProgress(state.loading)
                mButton.isEnabled = isViewEnabled(viewModel.isValid.value ?: true, it)
            }
        }

        viewModel.isValid.observe(viewLifecycleOwner) {
            mButton.isEnabled = isViewEnabled(it, viewModel.button.value)
        }

        view.setOnClickListener {
            dispatchFormEvent(FormActionEvent.SubmitPressed())
        }
    }

    private fun isViewEnabled(isValid: Boolean, button: ButtonState?): Boolean {
        return isValid && button?.loading == false
    }
}
