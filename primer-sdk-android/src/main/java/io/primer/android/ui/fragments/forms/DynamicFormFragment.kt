package io.primer.android.ui.fragments.forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding

import io.primer.android.R
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.databinding.FragmentDynamicFormBinding
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.components.TextInputWidget

import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class DynamicFormFragment : BaseFormFragment() {

    private var binding: FragmentDynamicFormBinding by autoCleaned()

    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDynamicFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButton()
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        val parentLayout = binding.mainLayout
        val inputs = form.inputs?.map { formData ->
            val childView = LayoutInflater.from(requireContext())
                .inflate(R.layout.payment_method_dynamic_input, parentLayout, false)
                as TextInputWidget

            childView.apply {
                id = View.generateViewId()
                hint = getString(formData.hint)

                editText?.inputType = formData.inputType

                setupEditTextTheme()
                setupEditTextInputFilters(
                    formData.inputCharacters,
                    formData.maxInputLength
                )
                setupEditTextListeners()
                onValueChanged = {
                    viewModel.onInputChanged(
                        formData.id,
                        formData.formType,
                        it,
                        formData.regex
                    )
                }
            }
        }

        inputs?.forEach {
            parentLayout.addView(it)
        }
        parentLayout.requestLayout()

        FieldFocuser.focus(inputs?.first()?.editText)
    }

    private fun setupButton() {
        val nextButton = binding.formButton
        nextButton.text = getString(R.string.confirm)
        nextButton.isEnabled = false

        viewModel.validationLiveData.observe(viewLifecycleOwner) { validated ->
            nextButton.isEnabled = validated
        }

        nextButton.setOnClickListener {
            val descriptor =
                primerViewModel.selectedPaymentMethod.value as AsyncPaymentMethodDescriptor
            viewModel.collectData().forEach {
                descriptor.appendTokenizableValue("sessionInfo", it.first, it.second.toString())
            }
            descriptor.behaviours.forEach {
                primerViewModel.executeBehaviour(it)
            }
        }
    }

    companion object {
        fun newInstance() = DynamicFormFragment()
    }
}
