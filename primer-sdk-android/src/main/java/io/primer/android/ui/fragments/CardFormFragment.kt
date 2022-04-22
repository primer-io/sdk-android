package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.PaymentMethodIntent
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.PrimerSessionIntent
import io.primer.android.SessionState
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.databinding.FragmentCardFormBinding
import io.primer.android.di.DIAppComponent
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.model.MonetaryAmount
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.payment.card.CARD_POSTAL_CODE_FIELD_NAME
import io.primer.android.ui.CardType
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.fragments.country.SelectCountryFragment
import io.primer.android.ui.utils.setMarginTopForError
import io.primer.android.utils.PaymentUtils
import io.primer.android.utils.hideKeyboard
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalCoroutinesApi
@KoinApiExtension
internal class CardFormFragment : BaseFragment() {

    private var cardInputFields: TreeMap<PrimerInputFieldType, TextInputWidget> by autoCleaned()
    private var binding: FragmentCardFormBinding by autoCleaned()

    private val tokenizationViewModel: TokenizationViewModel by viewModel()
    private val localConfig: PrimerConfig by inject()
    private val dirtyMap: MutableMap<String, Boolean> = HashMap()
    private var firstMount: Boolean = true
    private var network: CardType.Descriptor? = null
    private val networkAsString: String get() = network?.type.toString()
    private var isBeingDismissed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCardFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isBeingDismissed = false

        bindViewComponents()
        configureTokenizationObservers()
        addListenerAndSetState()
        tokenizationViewModel.resetPaymentMethod(primerViewModel.selectedPaymentMethod.value)
        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)

        primerViewModel.showBillingFields.observe(viewLifecycleOwner) { billingFields ->
            binding.billingAddressForm.onHandleAvailable(billingFields)

            renderInputFields()
            configureActionDone()

            validateAndShowErrorFields()
        }

        primerViewModel.showCardInformation.observe(viewLifecycleOwner) { cardInformation ->
            displayAvailableCardFields(cardInformation)

            renderInputFields()
            renderCardNumberInput()
            configureActionDone()

            /**
             * Handle for holder name into @see [io.primer.android.payment.card.CreditCard] model
             */
            tokenizationViewModel.setCardHasFields(cardInformation)

            validateAndShowErrorFields()
        }

        primerViewModel.orderCountry.observe(viewLifecycleOwner) { countryCode ->
            binding.billingAddressForm.onLoadCountry(countryCode)
        }

        primerViewModel.selectCountryCode.observe(viewLifecycleOwner) { country ->
            country ?: return@observe
            binding.billingAddressForm.onSelectCountry(country)
        }

        renderTitle()
        renderCancelButton()
        renderSubmitButton()
        if (!tokenizationViewModel.hasField(PrimerInputFieldType.CARD_NUMBER)) focusFirstInput()
        addInputFieldListeners(cardInputFields)
        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun validateAndShowErrorFields() {
        val cardErrors = primerViewModel.selectedPaymentMethod.value?.validate()
        val billingAddressErrors = primerViewModel.validateBillingAddress()

        tokenizationViewModel.validationErrors.postValue(
            billingAddressErrors.plus(cardErrors.orEmpty())
        )
    }

    /**
     * @see [cardInputFields] contains all fields, but need to filter for only available fields
     * and handle type @see [PrimerInputFieldType.ALL] when all fields is available
     * else remove from input fields and hide on layout.
     */
    private fun displayAvailableCardFields(cardInfoFields: Map<String, Boolean>?) {
        if (cardInfoFields == null) return
        if (cardInfoFields[PrimerInputFieldType.ALL.field] == true) return
        for ((fieldType, available) in cardInfoFields) {
            PrimerInputFieldType.fieldOf(fieldType)?.let { type ->
                if (!available) {
                    cardInputFields[type]?.isVisible = false
                    cardInputFields.remove(type)
                }
            }
        }
    }

    // bind view components
    private fun bindViewComponents() {
        cardInputFields = TreeMap()
        cardInputFields.putAll(
            /**
             * All inputs from UI with safe a sequence, for handle focus of keyboard
             * and IME option.
             */
            arrayOf(
                PrimerInputFieldType.CARD_NUMBER to binding.cardFormCardNumber,
                PrimerInputFieldType.EXPIRY_DATE to binding.cardFormCardExpiry,
                PrimerInputFieldType.CVV to binding.cardFormCardCvv,
                PrimerInputFieldType.CARDHOLDER_NAME to binding.cardFormCardholderName,
            )
        )
    }

    /*
    *
    * title
    *
    */

    private fun renderTitle() {
        val textColor = theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        binding.cardFormTitle.setTextColor(textColor)
    }

    /*
    *
    * cancel button
    *
    */

    private fun renderCancelButton() {
        binding.navCancelButton.apply {
            val textColor = theme.systemText.defaultColor.getColor(
                requireContext(),
                theme.isDarkMode
            )
            setTextColor(textColor)

            val fontSize = theme.systemText.fontsize.getDimension(requireContext())
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            setOnClickListener {
                primerViewModel.addAnalyticsEvent(
                    UIAnalyticsParams(
                        AnalyticsAction.CLICK,
                        ObjectType.BUTTON,
                        localConfig.toPlace(),
                        ObjectId.BACK
                    )
                )
                parentFragmentManager.popBackStack()
                primerViewModel.clearSelectedCountry()
                isBeingDismissed = true
            }
        }
    }

    /*
    *
    * input fields
    *
    */

    private fun renderInputFields() {
        cardInputFields.values.forEach { inputFieldView ->
            val fontSize = theme.input.text.fontsize.getDimension(requireContext())
            inputFieldView.editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            val color = theme.input.text.defaultColor.getColor(requireContext(), theme.isDarkMode)
            inputFieldView.editText?.setTextColor(color)
            inputFieldView.setupEditTextTheme()
            inputFieldView.setupEditTextListeners()

            when (theme.inputMode) {
                PrimerTheme.InputMode.UNDERLINED -> setInputFieldPadding(inputFieldView)
                PrimerTheme.InputMode.OUTLINED -> Unit
            }
        }
    }

    private fun focusFirstInput() = FieldFocuser.focus(
        cardInputFields[PrimerInputFieldType.CARD_NUMBER]?.editText
    )

    private fun renderCardNumberInput() {
        val cardNumberInput = cardInputFields[PrimerInputFieldType.CARD_NUMBER]
        cardNumberInput?.editText?.addTextChangedListener(TextInputMask.CardNumber())
        cardNumberInput?.editText?.addTextChangedListener(afterTextChanged = ::onCardNumberInput)
        updateCardNumberInputIcon()
    }

    private fun onCardNumberInput(content: Editable?) {
        val newNetwork = CardType.lookup(content.toString())
        val isSameNetwork = network?.type?.equals(newNetwork.type) ?: false
        if (isSameNetwork) {
            return
        }

        network = newNetwork

        updateCardNumberInputIcon()

        if (!primerViewModel.surchargeDisabled) {
            updateCardNumberInputSuffix()
            emitCardNetworkAction()
        }
    }

    private fun emitCardNetworkAction() {
        val actionParams = if (network == null || network?.type == CardType.Type.UNKNOWN) {
            ActionUpdateUnselectPaymentMethodParams
        } else {
            ActionUpdateSelectPaymentMethodParams(
                PaymentMethodType.PAYMENT_CARD,
                networkAsString
            )
        }

        primerViewModel.dispatchAction(actionParams) {
            lifecycleScope.launchWhenStarted {
                updateSubmitButton()
            }
        }
    }

    private fun setInputFieldPadding(view: View) {
        val res = requireContext().resources
        val horizontalPadding = res
            .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)

        view.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun updateCardNumberInputIcon() {
        val resource = network?.getResource() ?: R.drawable.ic_generic_card
        val input = cardInputFields[PrimerInputFieldType.CARD_NUMBER] ?: return
        input.editText?.setCompoundDrawablesRelativeWithIntrinsicBounds(resource, 0, 0, 0)
    }

    private fun updateCardNumberInputSuffix() {
        val inputFrame = binding.cardFormCardNumber
        val surcharge = primerViewModel.findSurchargeAmount("PAYMENT_CARD", networkAsString)
        val currency = localConfig.settings.order.currency
        val amount = MonetaryAmount.create(currency, surcharge)
        val amountText = "+" + PaymentUtils.amountToCurrencyString(amount)
        val surchargeText = if (surcharge > 0) amountText else ""
        inputFrame.suffixText = surchargeText
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addInputFieldListeners(fields: Map<PrimerInputFieldType, TextInputWidget>) {
        fields[PrimerInputFieldType.EXPIRY_DATE]?.editText?.addTextChangedListener(
            TextInputMask.ExpiryDate()
        )
        fields[PrimerInputFieldType.CARD_NUMBER]?.editText?.addTextChangedListener(
            TextInputMask.CardNumber()
        )
        fields.entries.forEach {
            it.value.editText?.addTextChangedListener(createCardInfoTextWatcher(it.key.field))
            it.value.editText?.onFocusChangeListener = createFocusChangeListener(it.key.field)
        }

        binding.billingAddressForm.fieldsMap().entries.forEach {
            if (it.key != PrimerInputFieldType.COUNTRY_CODE) {
                it.value.editText?.onFocusChangeListener = createFocusChangeListener(it.key.field)
            }
        }
        binding.billingAddressForm.onChooseCountry = { navigateToCountryChooser() }
        binding.billingAddressForm.onHideKeyboard = { activity?.hideKeyboard() }
        binding.billingAddressForm.onInputChange = { fieldType, value ->
            primerViewModel.billingAddressFields.value?.put(fieldType, value)
            validateAndShowErrorFields()
        }
    }

    private fun navigateToCountryChooser() {
        primerViewModel.navigateTo(
            NewFragmentBehaviour(
                { SelectCountryFragment.newInstance() },
                returnToPreviousOnBack = true
            )
        )
    }

    private fun configureActionDone() {
        val fieldsItem = mutableListOf<TextInputWidget>()
        fieldsItem.addAll(cardInputFields.values.filter { it.isVisible })
        fieldsItem.addAll(binding.billingAddressForm.fields())
        fieldsItem.forEach { it.editText?.imeOptions = EditorInfo.IME_ACTION_NEXT }
        fieldsItem.lastOrNull()?.let { lastInputField ->
            lastInputField.editText?.imeOptions = EditorInfo.IME_ACTION_DONE
            lastInputField.editText?.setOnEditorActionListener { _, _, _ ->
                binding.btnSubmitForm.performClick()
            }
        }
    }

    /**
     * Submit button
     */
    private fun renderSubmitButton() {
        val uxMode = localConfig.paymentMethodIntent
        val context = requireContext()

        binding.cardFormSubmitButton.text = when (uxMode) {
            PrimerSessionIntent.VAULT -> context.getString(R.string.add_card)
            PrimerSessionIntent.CHECKOUT -> {
                String
                    .format(
                        getString(R.string.pay_specific_amount),
                        PayAmountText.generate(context, primerViewModel.monetaryAmount)
                    )
            }
        }

        binding.cardFormSubmitButton.setOnClickListener {
            onSubmitButtonPressed()
        }
    }

    private fun onSubmitButtonPressed() {
        if (!tokenizationViewModel.isValid()) return
        tokenizationViewModel.tokenizationStatus.postValue(TokenizationStatus.LOADING)
        primerViewModel.emitBillingAddress { error ->
            if (error != null) {
                tokenizationViewModel.tokenizationStatus.postValue(TokenizationStatus.ERROR)
                return@emitBillingAddress
            }
            primerViewModel.addAnalyticsEvent(
                UIAnalyticsParams(
                    AnalyticsAction.CLICK,
                    ObjectType.BUTTON,
                    localConfig.toPlace(),
                    ObjectId.PAY
                )
            )
            tokenizationViewModel.tokenize()
        }
        activity?.hideKeyboard()
    }

    private fun updateSubmitButton() {
        if (localConfig.paymentMethodIntent == PrimerSessionIntent.VAULT) {
            binding.btnSubmitForm.text = getString(R.string.add_card)
            return
        }
        val amount = localConfig.getMonetaryAmountWithSurcharge()
        val amountString = PayAmountText.generate(requireContext(), amount)
        binding.btnSubmitForm.text = getString(R.string.pay_specific_amount, amountString)
    }

    // other configuration

    private fun configureTokenizationObservers() {
        // submit button loading status
        tokenizationViewModel.tokenizationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                TokenizationStatus.LOADING, TokenizationStatus.SUCCESS -> toggleLoading(true)
                else -> toggleLoading(false)
            }
        }

        tokenizationViewModel.tokenizationError.observe(viewLifecycleOwner) {
            binding.cardFormErrorMessage.apply {
                text = requireContext().getText(R.string.payment_method_error)
                isVisible = true
            }
        }

        tokenizationViewModel.validationErrors.observe(viewLifecycleOwner) {
            setValidationErrors()
        }

        tokenizationViewModel.autoFocusFields.observe(viewLifecycleOwner) {
            findNextFocusIfNeeded(it)
        }

        tokenizationViewModel.submitted.observe(viewLifecycleOwner) {
            setValidationErrors()
        }
    }

    private fun addListenerAndSetState() {
        primerViewModel.state.observe(viewLifecycleOwner) { state -> setBusy(state.showLoadingUi) }
        primerViewModel.setState(SessionState.AWAITING_USER)
    }

    private fun setBusy(isBusy: Boolean) {
        updateSubmitButton()
        if (isBusy) binding.btnSubmitForm.isEnabled = false
    }

    private fun toggleLoading(on: Boolean) {
        binding.btnSubmitForm.setProgress(on)
        if (on) binding.cardFormErrorMessage.isInvisible = true
        cardInputFields.values
            .plus(binding.billingAddressForm.fields())
            .forEach {
                it.isEnabled = on.not()
                it.alpha = if (on) 0.5f else 1.0f
            }
    }

    private fun createCardInfoTextWatcher(name: String): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tokenizationViewModel.setTokenizableValue(name, s.toString())
            }
        }
    }

    private fun createFocusChangeListener(name: String): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { _, hasFocus ->
            var skip = false

            if (!hasFocus && firstMount && name == PrimerInputFieldType.CARD_NUMBER.field) {
                firstMount = false
                skip = true
            }

            if (!skip && !hasFocus) {
                dirtyMap[name] = true
            }

            setValidationErrors()
        }
    }

    private fun findNextFocusIfNeeded(fields: Set<String>) {
        val currentFocus = cardInputFields.entries.firstOrNull { it.value.hasFocus() }
        val isValid =
            fields.isNotEmpty() && !firstMount &&
                fields.firstOrNull { currentFocus?.key?.field == it } != null
        if (isValid) {
            when (currentFocus?.key) {
                PrimerInputFieldType.CARD_NUMBER ->
                    FieldFocuser.focus(cardInputFields[PrimerInputFieldType.EXPIRY_DATE])
                PrimerInputFieldType.EXPIRY_DATE ->
                    FieldFocuser.focus(cardInputFields[PrimerInputFieldType.CVV])
                PrimerInputFieldType.CARDHOLDER_NAME -> binding.billingAddressForm.findNextFocus()
                PrimerInputFieldType.CVV -> {
                    when {
                        primerViewModel.showCardInformation.value
                            .via(PrimerInputFieldType.CARDHOLDER_NAME) == true ->
                            takeFocusCardholderName()
                        else -> binding.billingAddressForm.findNextFocus()
                    }
                }
            }
        }
    }

    private fun takeFocusCardholderName() {
        val cardInformation = primerViewModel.showCardInformation.value
        if (cardInformation.via(PrimerInputFieldType.CARDHOLDER_NAME) == true
        ) FieldFocuser.focus(
            cardInputFields[PrimerInputFieldType.CARDHOLDER_NAME]
        )
    }

    private fun setValidationErrors() {
        val tokenizationStatus = tokenizationViewModel.tokenizationStatus.value
        val errors = tokenizationViewModel.validationErrors.value ?: Collections.emptyList()

        binding.btnSubmitForm.isEnabled =
            errors.isEmpty() && isSubmitButtonEnabled(tokenizationStatus)

        val showAll = tokenizationViewModel.submitted.value == true

        cardInputFields.plus(binding.billingAddressForm.fieldsMap()).entries.forEach {
            val dirty = getIsDirty(it.key.field)
            val focused = it.value.isFocused

            if (showAll || dirty) {
                setValidationErrorState(
                    it.value,
                    if (focused) null else errors.find { err -> err.name == it.key.field },
                    it.key,
                )
            }
        }
    }

    private fun setValidationErrorState(
        input: TextInputWidget,
        error: SyncValidationError?,
        type: PrimerInputFieldType,
    ) {
        val isEnableError = error != null
        input.isErrorEnabled = isEnableError
        input.setMarginTopForError(isEnableError)
        if (error == null) input.error = error
        else requireContext()
            .let {
                input.error = it.getString(error.errorId, it.getString(error.fieldId))
                primerViewModel.addAnalyticsEvent(
                    MessageAnalyticsParams(
                        MessageType.VALIDATION_FAILED,
                        input.error.toString(),
                        Severity.WARN
                    )
                )
            }
            .run {
                if (type == PrimerInputFieldType.CARD_NUMBER) {
                    val inputFrame = binding.cardFormCardNumber
                    inputFrame.suffixText = ""
                }
            }
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        val hasFocus = cardInputFields.entries.any { it.value.isFocused }
        if (hasFocus && !visible) cardInputFields.entries.forEach { it.value.clearFocus() }
        else if (visible && !hasFocus &&
            !tokenizationViewModel.hasField(PrimerInputFieldType.CARD_NUMBER)
        ) focusFirstInput()
    }

    private fun getIsDirty(name: String): Boolean {
        return dirtyMap[name] ?: false
    }

    private fun isSubmitButtonEnabled(tokenizationStatus: TokenizationStatus?): Boolean {
        return tokenizationStatus == TokenizationStatus.NONE ||
            tokenizationStatus == TokenizationStatus.ERROR
    }

    override fun onDestroy() {
        primerViewModel.clearSelectedCountry()
        super.onDestroy()
    }

    companion object {

        @JvmStatic
        fun newInstance(): CardFormFragment {
            return CardFormFragment()
        }
    }
}
