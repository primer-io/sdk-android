package io.primer.android.ui.fragments

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
import androidx.fragment.app.activityViewModels
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.R
import io.primer.android.PaymentMethodIntent
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
import io.primer.android.utils.PaymentUtils
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import java.util.Collections
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalCoroutinesApi
@KoinApiExtension
internal class CardFormFragment : Fragment(), DIAppComponent {

    // view components
    private var inputLayouts: MutableMap<String, TextInputWidget> by autoCleaned()
    private var binding: FragmentCardFormBinding by autoCleaned()

    private val primerViewModel: PrimerViewModel by activityViewModels()
    private val tokenizationViewModel: TokenizationViewModel by viewModel()
    private val localConfig: PrimerConfig by inject()
    private val theme: PrimerTheme by inject()
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

        primerViewModel.showPostalCode.observe(viewLifecycleOwner) { showZipCode ->
            if (showZipCode) {
                inputLayouts[CARD_POSTAL_CODE_FIELD_NAME] = binding.cardFormPostalCode
            }
            configureActionDone()
            renderInputFields()
            inputLayouts[CARD_POSTAL_CODE_FIELD_NAME]?.isVisible = showZipCode
            tokenizationViewModel.setCardHasZipCode(showZipCode)
            tokenizationViewModel.validationErrors.postValue(
                primerViewModel.selectedPaymentMethod.value?.validate()
            )
        }

        primerViewModel.showCardholderName.observe(viewLifecycleOwner) { showCardholderName ->
            inputLayouts[CARD_NAME_FILED_NAME]?.isVisible = showCardholderName
            addInputFieldListeners()
            tokenizationViewModel.setCardHasCardholderName(showCardholderName)
            tokenizationViewModel.validationErrors.postValue(
                primerViewModel.selectedPaymentMethod.value?.validate()
            )
        }

        primerViewModel.orderCountry.observe(viewLifecycleOwner) { countryCode ->
            inputLayouts[CARD_POSTAL_CODE_FIELD_NAME]?.hint = when (countryCode) {
                CountryCode.US -> requireContext().getString(R.string.card_zip)
                else -> requireContext().getString(R.string.address_postal_code)
            }
        }

        renderTitle()
        renderCancelButton()
        renderSubmitButton()
        focusFirstInput()
        addInputFieldListeners()
    }

    // bind view components
    private fun bindViewComponents() {
        inputLayouts = mutableMapOf(
            CARD_NAME_FILED_NAME to binding.cardFormCardholderName,
            CARD_NUMBER_FIELD_NAME to binding.cardFormCardNumber,
            CARD_EXPIRY_FIELD_NAME to binding.cardFormCardExpiry,
            CARD_CVV_FIELD_NAME to binding.cardFormCardCvv,
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
            val textColor =
                theme.systemText.defaultColor.getColor(requireContext(), theme.isDarkMode)
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
        inputLayouts.values.forEach { t ->
            val fontSize = theme.input.text.fontsize.getDimension(requireContext())
            t.editText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            val color = theme.input.text.defaultColor.getColor(requireContext(), theme.isDarkMode)
            t.editText?.setTextColor(color)

            when (theme.inputMode) {
                PrimerTheme.InputMode.UNDERLINED -> setInputFieldPadding(t)
                PrimerTheme.InputMode.OUTLINED -> Unit
            }
        }
        addInputFieldListeners()
        renderCardNumberInput()
    }

    private fun focusFirstInput() = FieldFocuser.focus(
        inputLayouts[CARD_NUMBER_FIELD_NAME]?.editText
    )

    private fun renderCardNumberInput() {
        val cardNumberInput = inputLayouts[CARD_NUMBER_FIELD_NAME]
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
            activity?.runOnUiThread {
                updateSubmitButton()
            }
        }
    }

    private fun setInputFieldPadding(view: View) {
        val res = requireContext().resources
        val topPadding = res
            .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_top)
        val horizontalPadding = res
            .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)
        val bottomPadding = res
            .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_bottom)

        view.setPadding(
            horizontalPadding,
            topPadding,
            horizontalPadding,
            bottomPadding
        )
    }

    private fun updateCardNumberInputIcon() {
        val resource = network?.getResource() ?: R.drawable.ic_generic_card
        val input = inputLayouts[CARD_NUMBER_FIELD_NAME] ?: return
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

    private fun addInputFieldListeners() {
        inputLayouts[CARD_EXPIRY_FIELD_NAME]?.editText?.addTextChangedListener(
            TextInputMask.ExpiryDate()
        )
        inputLayouts[CARD_NUMBER_FIELD_NAME]?.editText?.addTextChangedListener(
            TextInputMask.CardNumber()
        )
        inputLayouts.entries.forEach {
            it.value.editText?.addTextChangedListener(createTextWatcher(it.key))
            it.value.editText?.onFocusChangeListener = createFocusChangeListener(it.key)
        }
        inputLayouts[CARD_POSTAL_CODE_FIELD_NAME]
            ?.editText?.addTextChangedListener { primerViewModel.setPostalCode(it.toString()) }
    }

    private fun configureActionDone() {
        inputLayouts[CARD_POSTAL_CODE_FIELD_NAME]?.editText?.let { postalCodeEditText ->
            postalCodeEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            postalCodeEditText.setOnEditorActionListener { _, _, _ ->
                binding.cardFormSubmitButton.performClick()
            }
            inputLayouts[CARD_NAME_FILED_NAME]?.editText?.let { cardNameEditText ->
                cardNameEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            return
        }
        inputLayouts[CARD_NAME_FILED_NAME]?.editText?.let { cardNameEditText ->
            cardNameEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            cardNameEditText.setOnEditorActionListener { _, _, _ ->
                binding.cardFormSubmitButton.performClick()
            }
        }
    }

    /*
    *
    * submit button
    * */

    private fun renderSubmitButton() {
        val uxMode = localConfig.paymentMethodIntent
        val context = requireContext()

        binding.cardFormSubmitButton.text = when (uxMode) {
            PaymentMethodIntent.VAULT -> context.getString(R.string.add_card)
            PaymentMethodIntent.CHECKOUT -> {
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
        primerViewModel.emitPostalCode {
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
    }

    private fun updateSubmitButton() {
        if (localConfig.paymentMethodIntent == PaymentMethodIntent.VAULT) {
            binding.cardFormSubmitButton.text = getString(R.string.add_card)
            return
        }
        // todo: rename, this has no effect
        val amount = localConfig.getMonetaryAmountWithSurcharge()
        val amountString = PayAmountText.generate(requireContext(), amount)
        val label = getString(R.string.pay_specific_amount)
        binding.cardFormSubmitButton.text = String.format(label, amountString)
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

        tokenizationViewModel
            .validationErrors
            .observe(viewLifecycleOwner) {
                setValidationErrors()
            }

        tokenizationViewModel
            .autoFocusFields
            .observe(viewLifecycleOwner) {
                findNextFocusIfNeeded(it)
            }

        tokenizationViewModel
            .submitted
            .observe(viewLifecycleOwner) { setValidationErrors() }
    }

    private fun addListenerAndSetState() {
        primerViewModel.state.observe(viewLifecycleOwner) { state -> setBusy(state.showLoadingUi) }
        primerViewModel.setState(SessionState.AWAITING_USER)
    }

    private fun setBusy(isBusy: Boolean) {
        updateSubmitButton()
        if (isBusy) binding.cardFormSubmitButton.isEnabled = false
    }

    private fun toggleLoading(on: Boolean) {
        binding.cardFormSubmitButton.setProgress(on)
        if (on) binding.cardFormErrorMessage.isInvisible = true
        inputLayouts.values.forEach {
            it.isEnabled = on.not()
            it.alpha = if (on) 0.5f else 1.0f
        }
    }

    private fun createTextWatcher(name: String): TextWatcher {
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

            if (
                !hasFocus &&
                !firstMount &&
                name == CARD_POSTAL_CODE_FIELD_NAME &&
                !isBeingDismissed
            ) {
                primerViewModel.emitPostalCode()
            }

            if (!hasFocus && firstMount && name == CARD_NUMBER_FIELD_NAME) {
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
        val currentFocus = inputLayouts.entries.firstOrNull { it.value.hasFocus() }
        val isValid =
            fields.isNotEmpty() && !firstMount &&
                fields.firstOrNull { currentFocus?.key == it } != null
        if (isValid) {
            when (currentFocus?.key) {
                CARD_NUMBER_FIELD_NAME -> FieldFocuser.focus(inputLayouts[CARD_EXPIRY_FIELD_NAME])
                CARD_EXPIRY_FIELD_NAME -> FieldFocuser.focus(inputLayouts[CARD_CVV_FIELD_NAME])
                CARD_CVV_FIELD_NAME -> {
                    when {
                        primerViewModel.showPostalCode.value == true -> FieldFocuser.focus(
                            inputLayouts[CARD_POSTAL_CODE_FIELD_NAME]
                        )
                        primerViewModel.showCardholderName.value == true -> FieldFocuser.focus(
                            inputLayouts[CARD_NAME_FILED_NAME]
                        )
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setValidationErrors() {
        val tokenizationStatus = tokenizationViewModel.tokenizationStatus.value
        val errors = tokenizationViewModel.validationErrors.value ?: Collections.emptyList()

        binding.cardFormSubmitButton.isEnabled =
            errors.isEmpty() && isSubmitButtonEnabled(tokenizationStatus)

        val showAll = tokenizationViewModel.submitted.value == true

        inputLayouts.entries.forEach {
            val dirty = getIsDirty(it.key)
            val focused = it.value.isFocused

            if (showAll || dirty) {
                setValidationErrorState(
                    it.value,
                    if (focused) null else errors.find { err -> err.name == it.key },
                    it.key,
                )
            }
        }
    }

    private fun setValidationErrorState(
        input: TextInputWidget,
        error: SyncValidationError?,
        type: String,
    ) {
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
                if (type == CARD_NUMBER_FIELD_NAME) {
                    val inputFrame = binding.cardFormCardNumber
                    inputFrame.suffixText = ""
                }
            }
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        val hasFocus = inputLayouts.entries.any { it.value.isFocused }
        if (hasFocus && !visible) inputLayouts.entries.forEach { it.value.clearFocus() }
        else if (visible && !hasFocus) focusFirstInput()
    }

    private fun getIsDirty(name: String): Boolean {
        return dirtyMap[name] ?: false
    }

    private fun isSubmitButtonEnabled(tokenizationStatus: TokenizationStatus?): Boolean {
        return tokenizationStatus == TokenizationStatus.NONE ||
            tokenizationStatus == TokenizationStatus.ERROR
    }

    companion object {

        @JvmStatic
        fun newInstance(): CardFormFragment {
            return CardFormFragment()
        }
    }
}
