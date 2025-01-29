package io.primer.android.ui.fragments.forms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import io.primer.android.R
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.configuration.data.model.emojiFlag
import io.primer.android.core.di.extensions.inject
import io.primer.android.data.payments.forms.models.FormType
import io.primer.android.data.payments.forms.models.helper.DialCodeCountryPrefix
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PrimerFragmentDynamicFormBinding
import io.primer.android.domain.payments.forms.models.Form
import io.primer.android.domain.payments.forms.models.FormInput
import io.primer.android.otp.PrimerOtpData
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.phoneNumber.PrimerPhoneNumberData
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.forms.binding.BaseFormBinding
import io.primer.android.ui.fragments.forms.binding.toBaseFormBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Suppress("NestedBlockDepth")
@ExperimentalCoroutinesApi
internal class DynamicFormFragment : BaseFormFragment(), PrimerHeadlessUniversalCheckoutRawDataManagerListener {
    private val localConfig: PrimerConfig by inject()

    private var binding: PrimerFragmentDynamicFormBinding by autoCleaned()
    private lateinit var rawDataManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
    override val baseFormBinding: BaseFormBinding by autoCleaned { binding.toBaseFormBinding() }

    private val descriptor: PaymentMethodDropInDescriptor
        get() = primerViewModel.selectedPaymentMethod.value as PaymentMethodDropInDescriptor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentDynamicFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        rawDataManager = PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(descriptor.paymentMethodType)
        rawDataManager.setListener(this)
        setupFormButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Will be uninitialized when returning to app after process death, due to onViewCreated() not being called.
        if (::rawDataManager.isInitialized) {
            rawDataManager.cleanup()
        }
    }

    override fun onValidationChanged(
        isValid: Boolean,
        errors: List<PrimerInputValidationError>,
    ) {
        binding.formButton.isEnabled = isValid
    }

    override fun setupForm(form: Form) {
        super.setupForm(form)
        val parentLayout = binding.mainLayout.also { it.removeAllViews() }

        val inputs =
            form.inputs?.map { formData ->
                val childView =
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.primer_payment_method_dynamic_input, parentLayout, false)
                        as TextInputWidget
                handleFormInputPrefix(childView, formData)

                childView
                    .apply {
                        id = View.generateViewId()
                        hint = getString(formData.hint)
                        editText?.inputType = formData.inputType
                        onValueChanged = createValueChangedListener()
                    }.also {
                        it.setupEditTextTheme(withTextPrefix = formData.formType == FormType.PHONE)
                        it.setupEditTextInputFilters(
                            formData.inputCharacters,
                            formData.maxInputLength,
                        )
                        it.setupEditTextListeners()
                    }
            }
        inputs?.forEach {
            parentLayout.addView(it)
        }

        parentLayout.requestLayout()
        binding.formButton.text = primerViewModel.getTotalAmountFormatted()

        FieldFocuser.focus(inputs?.first()?.editText)
    }

    private fun TextInputWidget.createValueChangedListener(): (CharSequence?) -> Unit =
        {
            when (descriptor.paymentMethodType) {
                PaymentMethodType.XENDIT_OVO.name,
                PaymentMethodType.ADYEN_MBWAY.name,
                -> {
                    val digits = "${prefixText ?: ""} $it".keepDigits()
                    rawDataManager.setRawData(PrimerPhoneNumberData(phoneNumber = "+$digits"))
                }

                PaymentMethodType.ADYEN_BLIK.name -> rawDataManager.setRawData(PrimerOtpData(it.toString()))

                else -> error("Unsupported payment method '${descriptor.paymentMethodType}'")
            }
        }

    private fun handleFormInputPrefix(
        childView: TextInputWidget,
        formData: FormInput,
    ) {
        when (val prefixData = formData.inputPrefix) {
            is DialCodeCountryPrefix -> {
                childView.prefixText =
                    String.format(
                        localConfig.settings.locale,
                        "%s %s",
                        prefixData.phoneCode.code.emojiFlag(),
                        prefixData.phoneCode.dialCode,
                    )
                childView.addDialCodeCountryPrefixDivider()
            }
        }
    }

    private fun setupFormButton() {
        with(binding.formButton) {
            isEnabled = false
            text = getString(R.string.confirm)
            setOnClickListener {
                showProgress()
                binding.mainLayout.children.forEach { it.isEnabled = false }

                logAnalyticsSubmit(descriptor.paymentMethodType)

                rawDataManager.submit()

                descriptor.behaviours.forEach {
                    primerViewModel.executeBehaviour(it)
                }
            }
        }
    }

    private fun logAnalyticsSubmit(paymentMethodType: String) =
        viewModel.addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                Place.DYNAMIC_FORM,
                ObjectId.SUBMIT,
                PaymentMethodContextParams(paymentMethodType),
            ),
        )

    // region Utils

    private fun String.keepDigits(): String = replace(Regex("\\D"), "")

    private fun TextInputWidget.addDialCodeCountryPrefixDivider() {
        prefixTextView.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(requireContext(), R.drawable.divider_input_prefix),
            null,
        )
    }
    // endregion

    companion object {
        fun newInstance() = DynamicFormFragment()
    }
}
