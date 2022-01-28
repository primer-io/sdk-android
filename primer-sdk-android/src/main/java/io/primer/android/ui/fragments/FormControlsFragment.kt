package io.primer.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.databinding.FragmentFormControlsBinding
import io.primer.android.ui.ButtonState
import io.primer.android.ui.extensions.autoCleaned
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class FormControlsFragment : FormChildFragment() {

    private var binding: FragmentFormControlsBinding by autoCleaned()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFormControlsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.button.observe(viewLifecycleOwner) {
            it?.let { state ->
                binding.formButtonCta.apply {
                    text = getString(state.labelId)
                    setProgress(state.loading)
                    isEnabled = isViewEnabled(viewModel.isValid.value ?: true, it)
                }
            }
        }

        viewModel.isValid.observe(viewLifecycleOwner) {
            binding.formButtonCta.isEnabled = isViewEnabled(it, viewModel.button.value)
        }

        view.setOnClickListener {
            dispatchFormEvent(FormActionEvent.SubmitPressed())
        }
    }

    private fun isViewEnabled(isValid: Boolean, button: ButtonState?): Boolean {
        return isValid && button?.loading == false
    }
}
