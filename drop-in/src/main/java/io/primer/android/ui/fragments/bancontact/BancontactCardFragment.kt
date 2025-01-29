package io.primer.android.ui.fragments.bancontact

import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import io.primer.android.R
import io.primer.android.bancontact.PrimerBancontactCardData
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManager
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.extensions.getSerializableExtraCompat
import io.primer.android.databinding.PrimerFragmentFormBancontactCardBinding
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.utils.hideKeyboard
import io.primer.android.utils.removeSpaces
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val CENTURY_START_YEAR = 2000
private const val CARD_EXPIRATION_SEPARATOR = '/'
private const val UPDATED_ELEMENT_TYPE_KEY = "updated_element_type"

@Suppress("TooManyFunctions")
@ExperimentalCoroutinesApi
internal class BancontactCardFragment : BaseFragment(), PrimerHeadlessUniversalCheckoutRawDataManagerListener {
    private lateinit var rawDataManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
    private var binding: PrimerFragmentFormBancontactCardBinding by autoCleaned()

    private val inputViews: MutableList<TextInputWidget> = mutableListOf()
    private val descriptor: PaymentMethodDropInDescriptor
        get() = primerViewModel.selectedPaymentMethod.value as PaymentMethodDropInDescriptor
    private var updatedElementType: PrimerInputElementType? = null
    private var network: CardNetwork.Descriptor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentFormBancontactCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        updatedElementType =
            savedInstanceState
                ?.getSerializableExtraCompat<PrimerInputElementType>(UPDATED_ELEMENT_TYPE_KEY)
        rawDataManager = PrimerHeadlessUniversalCheckoutRawDataManager.newInstance(descriptor.paymentMethodType)
        rawDataManager.setListener(this)
        setupComponents()
        setupTheme()
        setupInputs()
        setupListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(UPDATED_ELEMENT_TYPE_KEY, updatedElementType)
    }

    private fun setupComponents() {
        inputViews.add(binding.cardFormCardNumber)
        inputViews.add(binding.cardFormCardExpiry)
        inputViews.add(binding.cardFormCardholderName)

        updateCardNumberInputIcon()
        updateSubmitButton()
    }

    private fun setupTheme() {
        getToolbar()?.showOnlyLogo(R.drawable.ic_logo_bancontact)
        getToolbar()?.getBackButton()?.isVisible =
            primerViewModel.selectedPaymentMethod.value?.uiOptions
                ?.isStandalonePaymentMethod?.not()
                ?: false

        inputViews.forEach { inputView ->
            val fontSize = theme.input.text.fontSize.getDimension(requireContext())
            inputView.editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            val color = theme.input.text.defaultColor.getColor(requireContext(), theme.isDarkMode)
            inputView.editText?.setTextColor(color)
            inputView.setupEditTextTheme()
            inputView.setupEditTextListeners()

            when (theme.inputMode) {
                PrimerTheme.InputMode.UNDERLINED -> setInputFieldPadding(inputView)
                PrimerTheme.InputMode.OUTLINED -> Unit
            }
        }
    }

    private fun setInputFieldPadding(view: View) {
        val res = requireContext().resources
        val horizontalPadding =
            res
                .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)

        view.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun setupInputs() {
        binding.cardFormCardExpiry.editText?.addTextChangedListener(
            TextInputMask.ExpiryDate(),
        )
        binding.cardFormCardNumber.editText?.addTextChangedListener(
            TextInputMask.CardNumber(),
        )
    }

    private fun setupListeners() {
        getToolbar()?.getBackButton()?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnPay.setOnClickListener {
            it.hideKeyboard()
            updatedElementType = null
            updateRawData()
        }

        binding.cardFormCardNumber.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatedElementType = PrimerInputElementType.CARD_NUMBER
                updateRawData()
            }
        }
        binding.cardFormCardExpiry.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatedElementType = PrimerInputElementType.EXPIRY_DATE
                updateRawData()
            }
        }
        binding.cardFormCardholderName.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatedElementType = PrimerInputElementType.CARDHOLDER_NAME
                updateRawData()
            }
        }

        binding.cardFormCardNumber.editText?.addTextChangedListener(
            afterTextChanged = ::onCardNumberInputChanged,
        )

        binding.cardFormCardExpiry.editText?.addTextChangedListener {
            binding.cardFormCardExpiry.removeError()
            updateSubmitButton()
        }

        binding.cardFormCardholderName.editText?.addTextChangedListener {
            binding.cardFormCardholderName.removeError()
            updateSubmitButton()
        }
    }

    private fun updateRawData() {
        val rawExpiryDate = binding.cardFormCardExpiryInput.text?.toString().orEmpty().trim()
        val expiryDate =
            StringBuilder(rawExpiryDate.substringBefore(CARD_EXPIRATION_SEPARATOR)).apply {
                if (isNotBlank()) {
                    append(CARD_EXPIRATION_SEPARATOR)
                    append(
                        CENTURY_START_YEAR +
                            (rawExpiryDate.substringAfterLast(CARD_EXPIRATION_SEPARATOR).toIntOrNull() ?: 0),
                    )
                }
            }
        rawDataManager.setRawData(
            PrimerBancontactCardData(
                cardNumber = binding.cardFormCardNumberInput.text?.toString().orEmpty().trim().removeSpaces(),
                expiryDate = expiryDate.toString(),
                cardHolderName = binding.cardFormCardholderNameInput.text?.toString().orEmpty().trim(),
            ),
        )
    }

    private fun onCardNumberInputChanged(content: Editable?) {
        val newNetwork = CardNetwork.lookup(content.toString())
        val isSameNetwork = network?.type?.equals(newNetwork.type) ?: false
        binding.cardFormCardNumber.removeError()

        if (isSameNetwork) {
            return
        }

        network = newNetwork

        updateCardNumberInputIcon()
        updateSubmitButton()
    }

    private fun updateSubmitButton() {
        val amountString = primerViewModel.getTotalAmountFormatted()
        binding.btnPay.text = getString(R.string.pay_specific_amount, amountString)
        binding.btnPay.isEnabled = !hasInputErrors()
    }

    private fun hasInputErrors(): Boolean =
        binding.cardFormCardNumber.isErrorEnabled ||
            binding.cardFormCardExpiry.isErrorEnabled ||
            binding.cardFormCardholderName.isErrorEnabled

    private fun updateCardNumberInputIcon() {
        binding.cardFormCardNumber.editText?.setCompoundDrawablesRelativeWithIntrinsicBounds(
            network?.type?.getCardImageAsset(ImageColor.COLORED)
                ?: R.drawable.ic_generic_card,
            0,
            0,
            0,
        )
    }

    override fun onValidationChanged(
        isValid: Boolean,
        errors: List<PrimerInputValidationError>,
    ) {
        if (view == null) {
            // Fragment is destroyed, accessing binding is not possible
            return
        }

        if (isValid) {
            descriptor.behaviours.forEach {
                primerViewModel.executeBehaviour(it)
            }
            rawDataManager.submit()
            return
        }
        errors
            .filter {
                // consider all inputs when submitting the form
                updatedElementType == null ||
                    // consider only relevant input when reacting to value change
                    it.inputElementType == updatedElementType
            }
            .forEach { (_, _, inputElementType) ->
                val (inputLayout, inputNameResId) =
                    when (inputElementType) {
                        PrimerInputElementType.CARD_NUMBER ->
                            binding.cardFormCardNumber to R.string.card_number

                        PrimerInputElementType.EXPIRY_DATE ->
                            binding.cardFormCardExpiry to R.string.card_expiry

                        PrimerInputElementType.CARDHOLDER_NAME ->
                            binding.cardFormCardholderName to R.string.card_holder_name

                        else -> error("Unsupported element type $inputElementType")
                    }
                val inputText = inputLayout.editText?.text?.toString()
                val errorResId =
                    if (inputText.isNullOrBlank()) {
                        R.string.form_error_required
                    } else {
                        R.string.form_error_invalid
                    }
                val errorMessage = getString(errorResId, getString(inputNameResId))
                inputLayout.isErrorEnabled = true
                inputLayout.error = errorMessage
            }
    }

    companion object {
        fun newInstance() = BancontactCardFragment()
    }
}
