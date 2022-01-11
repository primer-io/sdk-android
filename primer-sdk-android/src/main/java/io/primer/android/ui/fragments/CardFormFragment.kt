package io.primer.android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.PaymentMethodIntent
import io.primer.android.SessionState
import io.primer.android.data.action.models.ClientSessionActionsRequest
import io.primer.android.di.DIAppComponent
import io.primer.android.model.dto.CountryCode
import io.primer.android.model.dto.MonetaryAmount
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.payment.card.CARD_CVV_FIELD_NAME
import io.primer.android.payment.card.CARD_EXPIRY_FIELD_NAME
import io.primer.android.payment.card.CARD_NAME_FILED_NAME
import io.primer.android.payment.card.CARD_NUMBER_FIELD_NAME
import io.primer.android.payment.card.CARD_POSTAL_CODE_FIELD_NAME
import io.primer.android.ui.CardType
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.PayAmountText
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.ButtonPrimary
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.utils.PaymentUtils
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.android.viewmodel.TokenizationViewModel
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
@KoinApiExtension
internal class CardFormFragment : Fragment(), DIAppComponent {

    // view components
    private lateinit var inputLayouts: MutableMap<String, TextInputWidget>
    private lateinit var submitButton: ButtonPrimary
    private lateinit var cancelButton: TextView
    private lateinit var title: TextView
    private lateinit var errorText: TextView

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
        return inflater.inflate(R.layout.fragment_card_form, container, false)
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
                val value: TextInputWidget = view.findViewById(R.id.card_form_postal_code)
                inputLayouts[CARD_POSTAL_CODE_FIELD_NAME] = value
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
        val view = view ?: return
        title = view.findViewById(R.id.card_form_title)
        cancelButton = view.findViewById(R.id.nav_cancel_button)

        inputLayouts = mutableMapOf(
            CARD_NAME_FILED_NAME to view.findViewById(R.id.card_form_cardholder_name),
            CARD_NUMBER_FIELD_NAME to view.findViewById(R.id.card_form_card_number),
            CARD_EXPIRY_FIELD_NAME to view.findViewById(R.id.card_form_card_expiry),
            CARD_CVV_FIELD_NAME to view.findViewById(R.id.card_form_card_cvv),
        )
        errorText = view.findViewById(R.id.card_form_error_message)
        submitButton = view.findViewById(R.id.card_form_submit_button)
    }

    /*
    *
    * title
    * */

    private fun renderTitle() {
        val textColor = theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        title.setTextColor(textColor)
    }

    /*
    *
    * cancel button
    * */

    private fun renderCancelButton() {
        val textColor = theme.systemText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        cancelButton.setTextColor(textColor)

        val fontSize = theme.systemText.fontsize.getDimension(requireContext())
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
            isBeingDismissed = true
        }
    }

    /*
    *
    * input fields
    * */

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
        val type = "PAYMENT_CARD"

        val action = if (network == null) {
            ClientSessionActionsRequest.UnsetPaymentMethod()
        } else {
            ClientSessionActionsRequest.SetPaymentMethod(type, networkAsString)
        }

        primerViewModel.dispatchAction(action) {
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
        val inputFrame = view?.findViewById<TextInputWidget>(R.id.card_form_card_number)
        val surcharge = primerViewModel.findSurchargeAmount("PAYMENT_CARD", networkAsString)
        val currency = localConfig.settings.order.currency
        val amount = MonetaryAmount.create(currency, surcharge)
        val amountText = "+" + PaymentUtils.amountToCurrencyString(amount)
        val surchargeText = if (surcharge > 0) amountText else ""
        inputFrame?.suffixText = surchargeText
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
                submitButton.performClick()
            }
            inputLayouts[CARD_NAME_FILED_NAME]?.editText?.let { cardNameEditText ->
                cardNameEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
            return
        }
        inputLayouts[CARD_NAME_FILED_NAME]?.editText?.let { cardNameEditText ->
            cardNameEditText.imeOptions = EditorInfo.IME_ACTION_DONE
            cardNameEditText.setOnEditorActionListener { _, _, _ ->
                submitButton.performClick()
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

        submitButton.text = when (uxMode) {
            PaymentMethodIntent.VAULT -> context.getString(R.string.add_card)
            PaymentMethodIntent.CHECKOUT -> {
                String
                    .format(
                        requireContext().getString(R.string.pay_specific_amount),
                        PayAmountText.generate(context, primerViewModel.monetaryAmount)
                    )
            }
        }

        submitButton.setOnClickListener {
            onSubmitButtonPressed()
        }
    }

    private fun onSubmitButtonPressed() {
        if (!tokenizationViewModel.isValid()) return
        tokenizationViewModel.tokenizationStatus.postValue(TokenizationStatus.LOADING)
        primerViewModel.emitPostalCode {
            tokenizationViewModel.tokenize()
        }
    }

    private fun updateSubmitButton() {
        if (localConfig.paymentMethodIntent == PaymentMethodIntent.VAULT) {
            submitButton.text = requireContext().getString(R.string.add_card)
            return
        }
        val surcharge = primerViewModel.findSurchargeAmount("PAYMENT_CARD", networkAsString)
        // todo: rename, this has no effect
        val amount = localConfig.getMonetaryAmountWithSurcharge(surcharge)
        val amountString = PayAmountText.generate(requireContext(), amount)
        val label = requireContext().getString(R.string.pay_specific_amount)
        submitButton.text = String.format(label, amountString)
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
            errorText.text = requireContext().getText(R.string.payment_method_error)
            errorText.visibility = View.VISIBLE
        }

        tokenizationViewModel
            .validationErrors
            .observe(viewLifecycleOwner) { setValidationErrors() }

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
        if (isBusy) submitButton.isEnabled = false
    }

    private fun toggleLoading(on: Boolean) {
        submitButton.setProgress(on)
        if (on) errorText.visibility = View.INVISIBLE
        inputLayouts.values.forEach {
            it.isEnabled = on.not()
            it.alpha = if (on) 0.5f else 1.0f
        }
    }

    private fun createTextWatcher(name: String): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

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

    private fun setValidationErrors() {
        val tokenizationStatus = tokenizationViewModel.tokenizationStatus.value
        val errors = tokenizationViewModel.validationErrors.value ?: Collections.emptyList()

        submitButton.isEnabled = errors.isEmpty() && isSubmitButtonEnabled(tokenizationStatus)

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
            .let { input.error = it.getString(error.errorId, it.getString(error.fieldId)) }
            .run {
                if (type == CARD_NUMBER_FIELD_NAME) {
                    val inputFrame = view?.findViewById<TextInputWidget>(R.id.card_form_card_number)
                    inputFrame?.suffixText = ""
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
