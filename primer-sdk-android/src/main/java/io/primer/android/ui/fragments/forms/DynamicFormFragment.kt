package io.primer.android.ui.fragments.forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding

import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.databinding.FragmentDynamicFormBinding
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.payment.async.AsyncPaymentMethodDescriptor
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import kotlinx.coroutines.ExperimentalCoroutinesApi
import io.primer.android.viewmodel.TokenizationStatus
import kotlinx.coroutines.flow.combineTransform
import org.koin.core.component.KoinApiExtension

@ExperimentalCoroutinesApi
@KoinApiExtension
internal class DynamicFormFragment : BaseFormFragment() {

    private var binding: FragmentDynamicFormBinding by autoCleaned()
    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    private val tokenizationStatusObserver = Observer<TokenizationStatus> { status ->
        binding.apply {
            val viewsEnabled = setOf(
                TokenizationStatus.NONE,
                TokenizationStatus.ERROR
            ).contains(status)
            mainLayout.children.forEach {
                it.isEnabled = viewsEnabled
            }
            formButton.setProgress(status != TokenizationStatus.NONE)
        }
    }

    private val formButtonEnabledLiveData by lazy {
        tokenizationViewModel.tokenizationStatus.asFlow()
            .combineTransform(viewModel.validationLiveData.asFlow()) { status, formValidated ->
                emit(
                    formValidated && setOf(
                        TokenizationStatus.NONE,
                        TokenizationStatus.ERROR
                    ).contains(status)
                )
            }.asLiveData()
    }

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
        parentLayout.removeAllViews()
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

        tokenizationViewModel.tokenizationStatus.observe(
            viewLifecycleOwner,
            tokenizationStatusObserver
        )
    }

    private fun setupButton() {
        val nextButton = binding.formButton
        nextButton.text = getString(R.string.confirm)

        formButtonEnabledLiveData.observe(viewLifecycleOwner) { validated ->
            nextButton.isEnabled = validated
        }

        nextButton.setOnClickListener {
            val descriptor =
                primerViewModel.selectedPaymentMethod.value as AsyncPaymentMethodDescriptor
            logAnalyticsSubmit(descriptor.config.type)

            viewModel.collectData().forEach {
                descriptor.appendTokenizableValue("sessionInfo", it.first, it.second.toString())
            }
            descriptor.behaviours.forEach {
                primerViewModel.executeBehaviour(it)
            }
        }
    }

    private fun logAnalyticsSubmit(paymentMethodType: PaymentMethodType) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                Place.DYNAMIC_FORM,
                ObjectId.SUBMIT,
                PaymentMethodContextParams(paymentMethodType)
            )
        )

    companion object {
        fun newInstance() = DynamicFormFragment()
    }
}
