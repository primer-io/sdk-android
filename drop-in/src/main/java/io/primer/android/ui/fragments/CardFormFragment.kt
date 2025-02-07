package io.primer.android.ui.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.primer.android.PrimerSessionIntent
import io.primer.android.R
import io.primer.android.SessionState
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.MessageType
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Severity
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.analytics.domain.models.MessageAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.extension.sanitizedCardNumber
import io.primer.android.core.di.extensions.inject
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.databinding.PrimerFragmentCardFormBinding
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.NewFragmentBehaviour
import io.primer.android.paymentMethods.core.ui.assets.AssetsManager
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.FieldFocuser
import io.primer.android.ui.TextInputMask
import io.primer.android.ui.components.CardPopupMenuAdapter
import io.primer.android.ui.components.TextInputWidget
import io.primer.android.ui.extensions.autoCleaned
import io.primer.android.ui.extensions.getDimensionAsPx
import io.primer.android.ui.fragments.base.BaseFragment
import io.primer.android.ui.fragments.country.SelectCountryFragment
import io.primer.android.ui.settings.PrimerTheme
import io.primer.android.ui.utils.setMarginBottomForError
import io.primer.android.utils.hideKeyboard
import io.primer.android.viewmodel.CardNetworksState
import io.primer.android.viewmodel.CardViewModel
import io.primer.android.viewmodel.TokenizationStatus
import io.primer.cardShared.extension.isCardHolderNameEnabled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.EnumMap
import java.util.TreeMap

/**
 * A simple [Fragment] subclass.
 * Use the [CardFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@ExperimentalCoroutinesApi
@Suppress("TooManyFunctions")
internal class CardFormFragment : BaseFragment() {
    private var cardInputFields: TreeMap<PrimerInputElementType, TextInputWidget> by autoCleaned()
    private var binding: PrimerFragmentCardFormBinding by autoCleaned()

    internal val cardViewModel: CardViewModel by viewModels()
    private val assetsManager: AssetsManager by inject()

    private val localConfig: PrimerConfig by inject()
    private val dirtyMap: MutableMap<PrimerInputElementType, Boolean> = EnumMap(PrimerInputElementType::class.java)
    private var firstMount: Boolean = true

    private var isBeingDismissed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            // Reset country selection when the card form is re-opened.
            primerViewModel.clearSelectedCountry()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PrimerFragmentCardFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        isBeingDismissed = false

        bindViewComponents()
        configureTokenizationObservers()
        addListenerAndSetState()
        cardViewModel.initialize()
        primerViewModel.keyboardVisible.observe(viewLifecycleOwner, ::onKeyboardVisibilityChanged)

        primerViewModel.showBillingFields.observe(viewLifecycleOwner) { billingFields ->
            binding.billingAddressForm.onHandleAvailable(billingFields)
            binding.billingAddressDivider.isVisible = billingFields.isNullOrEmpty().not()

            renderInputFields()
            configureActionDone()

            validateAndShowErrorFields()
        }

        primerViewModel.showCardInformation.observe(viewLifecycleOwner) { cardInformation ->
            displayAvailableCardFields(cardInformation?.options)

            renderInputFields()
            renderCardNumberInput()
            configureActionDone()
            validateAndShowErrorFields()
        }

        primerViewModel.selectedCountryCode.observe(viewLifecycleOwner) { country ->
            country ?: return@observe
            binding.billingAddressForm.onSelectCountry(country)
        }

        cardViewModel.cardNetworksState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { onCardNetworkStateChanged(it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        logValidationErrors()
        renderTitle()
        renderCardDetailsTitle()
        renderCancelButton()
        renderSubmitButton()
        focusFirstInput()
        addInputFieldListeners(cardInputFields)
        adjustBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    private fun validateAndShowErrorFields() {
        val billingAddressErrors = primerViewModel.validateBillingAddress()
        cardViewModel.updateValidationErrors(
            billingAddressErrors,
        )
    }

    /**
     * @see [cardInputFields] contains all fields, but need to filter for only available fields
     * and handle type @see [PrimerInputElementType.ALL] when all fields is available
     * else remove from input fields and hide on layout.
     */
    private fun displayAvailableCardFields(cardInfoFields: Map<String, Boolean>?) {
        if (cardInfoFields == null) return
        if (cardInfoFields[PrimerInputElementType.ALL.field] == true) return
        for ((fieldType, available) in cardInfoFields) {
            PrimerInputElementType.fieldOf(fieldType)?.let { type ->
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
            arrayOf(
                PrimerInputElementType.CARD_NUMBER to binding.cardFormCardNumber,
                PrimerInputElementType.EXPIRY_DATE to binding.cardFormCardExpiry,
                PrimerInputElementType.CVV to binding.cardFormCardCvv,
                PrimerInputElementType.CARDHOLDER_NAME to binding.cardFormCardholderName,
            ),
        )
    }

    private fun renderTitle() {
        getToolbar()?.showOnlyTitle(R.string.pay_with_card)
    }

    private fun renderCardDetailsTitle() {
        val textColor = theme.subtitleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        binding.tvCardDetailTitle.setTextColor(textColor)
        binding.billingAddressDivider.setBackgroundColor(textColor)
    }

    private fun renderCancelButton() {
        getToolbar()?.getBackButton()?.apply {
            isVisible = localConfig.isStandalonePaymentMethod.not()
            setOnClickListener {
                primerViewModel.addAnalyticsEvent(
                    UIAnalyticsParams(
                        AnalyticsAction.CLICK,
                        ObjectType.BUTTON,
                        localConfig.toPlace(),
                        ObjectId.BACK,
                    ),
                )
                parentFragmentManager.popBackStack()
                primerViewModel.clearSelectedCountry()
                isBeingDismissed = true
            }
        }
    }

    private fun renderInputFields() {
        cardInputFields.values.forEach { inputFieldView ->
            val fontSize = theme.input.text.fontSize.getDimension(requireContext())
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

    private fun focusFirstInput() =
        FieldFocuser.focus(
            cardInputFields[PrimerInputElementType.CARD_NUMBER]?.editText,
        )

    private fun renderCardNumberInput() {
        val cardNumberInput = cardInputFields[PrimerInputElementType.CARD_NUMBER]
        cardNumberInput?.editText?.addTextChangedListener(TextInputMask.CardNumber())
        cardNumberInput?.editText?.addTextChangedListener(afterTextChanged = { showSurchargeIfNeeded() })
    }

    private fun onCardNetworkStateChanged(state: CardNetworksState?) {
        if (state != null) {
            updateCardNetworkViews(
                state.networks.map { it.network },
                state.selectedNetwork,
            ) { network ->
                cardViewModel.setSelectedNetwork(network)
            }
        } else {
            updateCardNetworkViews(emptyList(), null) { }
        }
        showSurchargeIfNeeded()
        setValidationErrors()
    }

    private fun updateCardNetworkViews(
        networks: List<CardNetwork.Type>,
        selectedNetwork: CardNetwork.Type?,
        onNetworkSelected: (CardNetwork.Type) -> Unit,
    ) {
        when (networks.size) {
            0 -> showSingleCard(null)
            1 -> showSingleCard(networks.first())
            else -> showCoBadgeCardDropdown(networks, selectedNetwork ?: networks.first(), onNetworkSelected)
        }
    }

    private fun showSingleCard(networkType: CardNetwork.Type?) {
        with(binding) {
            cardNetworkContainer.setOnClickListener(null)
            imageViewCardNetwork.setImageDrawable(getCardNetworkDrawable(networkType))
            imageViewCardNetworkCaret.isVisible = false
        }
    }

    private fun showCoBadgeCardDropdown(
        networks: List<CardNetwork.Type>,
        selectedNetwork: CardNetwork.Type,
        onNetworkSelected: (CardNetwork.Type) -> Unit,
    ) {
        with(binding) {
            cardNetworkContainer.setOnClickListener {
                showCardSelectPopup(networks, selectedNetwork, onNetworkSelected)
            }
            imageViewCardNetwork.setImageDrawable(getCardNetworkDrawable(selectedNetwork))
            imageViewCardNetworkCaret.isVisible = true
        }
    }

    private fun showCardSelectPopup(
        networks: List<CardNetwork.Type>,
        selectedNetwork: CardNetwork.Type,
        onNetworkSelected: (CardNetwork.Type) -> Unit,
    ) {
        val offset = requireContext().getDimensionAsPx(R.dimen.primer_card_network_popup_offset)
        ListPopupWindow(requireContext()).apply {
            setAdapter(CardPopupMenuAdapter(requireContext(), networks, selectedNetwork))
            anchorView = binding.cardFormCardNumber
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(
                AppCompatResources.getDrawable(requireContext(), R.drawable.background_card_select_menu),
            )
            verticalOffset = offset
            setOnItemClickListener { _, _, position, _ ->
                onNetworkSelected(networks[position])
                dismiss()
            }
            view?.id = R.id.co_badge_popup
            show()
        }
    }

    private fun getCardNetworkDrawable(type: CardNetwork.Type?) =
        type?.let {
            assetsManager.getCardNetworkImage(requireContext(), it)
        } ?: AppCompatResources.getDrawable(requireContext(), R.drawable.ic_generic_card)

    private fun emitCardNetworkAction(cardType: CardNetwork.Type?) {
        val actionParams =
            if (cardType == null || cardType == CardNetwork.Type.OTHER) {
                ActionUpdateUnselectPaymentMethodParams
            } else {
                ActionUpdateSelectPaymentMethodParams(
                    PaymentMethodType.PAYMENT_CARD.name,
                    cardType.name,
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
        val horizontalPadding =
            res
                .getDimensionPixelSize(R.dimen.primer_underlined_input_padding_horizontal)

        view.setPadding(horizontalPadding, 0, horizontalPadding, 0)
    }

    private fun showSurchargeIfNeeded() {
        val cardType = cardViewModel.cardNetworksState.value.selectedNetwork
        val surchargeAmount = primerViewModel.findSurchargeAmount(PaymentMethodType.PAYMENT_CARD.name, cardType?.name)

        if (!primerViewModel.surchargeDisabled && cardType != null && surchargeAmount > 0) {
            showSurcharge(surchargeAmount)
            emitCardNetworkAction(cardType)
        } else {
            binding.textViewSurcharge.visibility = View.GONE
        }
    }

    private fun showSurcharge(surchargeAmount: Int) {
        val textColor = theme.titleText.defaultColor.getColor(requireContext(), theme.isDarkMode)
        val surchargeText = "+" + primerViewModel.getAmountFormatted(amount = surchargeAmount)
        with(binding.textViewSurcharge) {
            setTextColor(ColorStateList.valueOf(textColor))
            text = surchargeText
            visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addInputFieldListeners(fields: Map<PrimerInputElementType, TextInputWidget>) {
        fields[PrimerInputElementType.EXPIRY_DATE]?.editText?.addTextChangedListener(
            TextInputMask.ExpiryDate(),
        )
        fields[PrimerInputElementType.CARD_NUMBER]?.editText?.addTextChangedListener(
            TextInputMask.CardNumber(),
        )
        fields.entries.forEach {
            it.value.editText?.addTextChangedListener(createCardInfoTextWatcher())
            it.value.editText?.onFocusChangeListener = createFocusChangeListener(it.key)
        }

        binding.billingAddressForm.fieldsMap().entries.forEach {
            if (it.key != PrimerInputElementType.COUNTRY_CODE) {
                it.value.editText?.onFocusChangeListener = createFocusChangeListener(it.key)
            }
        }
        binding.billingAddressForm.onCountryFocus = { fieldName, hasFocus ->
            handleInputFocuseByName(fieldName, hasFocus)
        }
        binding.billingAddressForm.onChooseCountry = { navigateToCountryChooser() }
        binding.billingAddressForm.onInputChange = { fieldType, value ->
            primerViewModel.billingAddressFields.value?.put(fieldType, value)
            validateAndShowErrorFields()
        }
    }

    private fun navigateToCountryChooser() {
        primerViewModel.navigateTo(
            NewFragmentBehaviour(
                { SelectCountryFragment.newInstance() },
                returnToPreviousOnBack = true,
                tag = null,
            ),
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

    private fun renderSubmitButton() {
        val uxMode = localConfig.paymentMethodIntent
        val context = requireContext()

        binding.btnSubmitForm.text =
            when (uxMode) {
                PrimerSessionIntent.VAULT -> context.getString(R.string.add_card)
                PrimerSessionIntent.CHECKOUT -> {
                    String.format(getString(R.string.pay_specific_amount), primerViewModel.getTotalAmountFormatted())
                }
            }

        binding.btnSubmitForm.setOnClickListener { onSubmitButtonPressed() }
    }

    private fun onSubmitButtonPressed() {
        if (!cardViewModel.isValid()) return
        cardViewModel.tokenizationStatus.tryEmit(TokenizationStatus.LOADING)
        primerViewModel.emitBillingAddress { error ->
            if (error != null) {
                cardViewModel.tokenizationStatus.tryEmit(TokenizationStatus.ERROR)
                return@emitBillingAddress
            }
            primerViewModel.addAnalyticsEvent(
                UIAnalyticsParams(
                    AnalyticsAction.CLICK,
                    ObjectType.BUTTON,
                    localConfig.toPlace(),
                    ObjectId.PAY,
                ),
            )
            cardViewModel.submit()
        }
        view?.hideKeyboard()
    }

    private fun updateSubmitButton() {
        if (localConfig.paymentMethodIntent == PrimerSessionIntent.VAULT) {
            binding.btnSubmitForm.text = getString(R.string.add_card)
            return
        }
        val amountString = primerViewModel.getTotalAmountFormatted()
        binding.btnSubmitForm.text = getString(R.string.pay_specific_amount, amountString)
    }

    private fun configureTokenizationObservers() {
        // submit button loading status
        cardViewModel.tokenizationStatus
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { status ->
                when (status) {
                    TokenizationStatus.LOADING, TokenizationStatus.SUCCESS -> toggleLoading(true)
                    else -> toggleLoading(false)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        cardViewModel.cardValidationErrors
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { setValidationErrors() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        cardViewModel.billingAddressValidationErrors
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { setValidationErrors() }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        cardViewModel.autoFocusFields.observe(viewLifecycleOwner) {
            findNextFocusIfNeeded(it)
        }
    }

    private fun addListenerAndSetState() {
        primerViewModel.state.observe(viewLifecycleOwner) { state -> setBusy(state.showLoadingUi) }
        primerViewModel.setState(SessionState.AWAITING_USER)
    }

    private fun setBusy(isBusy: Boolean) {
        updateSubmitButton()
        binding.btnSubmitForm.isEnabled = isBusy.not() && cardViewModel.isValid()
    }

    private fun toggleLoading(on: Boolean) {
        binding.btnSubmitForm.setProgress(on)
        binding.btnSubmitForm.isEnabled = on.not()
        if (on) binding.cardFormErrorMessage.isInvisible = true
        cardInputFields.values
            .plus(binding.billingAddressForm.fields())
            .forEach {
                it.isEnabled = on.not()
                it.alpha = if (on) ALPHA_HALF else ALPHA_VISIBLE
            }
    }

    private fun createCardInfoTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int,
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int,
            ) {
                cardViewModel.onCardDataChanged(
                    PrimerCardData(
                        cardNumber =
                        cardInputFields[PrimerInputElementType.CARD_NUMBER]?.editText?.text.toString()
                            .sanitizedCardNumber(),
                        cvv = cardInputFields[PrimerInputElementType.CVV]?.editText?.text.toString(),
                        expiryDate =
                        cardInputFields[PrimerInputElementType.EXPIRY_DATE]?.editText?.text.toString()
                            .let {
                                val parts = it.split("/")
                                if (parts.size != 2) {
                                    it
                                } else {
                                    "${parts[0]}/20${parts[1]}"
                                }
                            },
                        cardHolderName =
                        cardInputFields[PrimerInputElementType.CARDHOLDER_NAME]?.editText?.text.toString(),
                    ),
                )
                validateAndShowErrorFields()
            }
        }
    }

    private fun createFocusChangeListener(inputElementType: PrimerInputElementType): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { _, hasFocus ->
            handleInputFocuseByName(inputElementType, hasFocus)
        }
    }

    private fun handleInputFocuseByName(
        inputElementType: PrimerInputElementType,
        hasFocus: Boolean,
    ) {
        var skip = false

        if (!hasFocus && firstMount && inputElementType == PrimerInputElementType.CARD_NUMBER) {
            firstMount = false
            skip = true
        }

        if (!skip && !hasFocus) {
            dirtyMap[inputElementType] = true
        }

        validateAndShowErrorFields()
    }

    private fun findNextFocusIfNeeded(fields: Set<PrimerInputElementType>) {
        val currentFocus = cardInputFields.entries.firstOrNull { it.value.hasFocus() }
        val isValid =
            fields.isNotEmpty() && !firstMount &&
                fields.firstOrNull { currentFocus?.key == it } != null
        if (isValid) {
            when (currentFocus?.key) {
                PrimerInputElementType.CARD_NUMBER ->
                    FieldFocuser.focus(cardInputFields[PrimerInputElementType.EXPIRY_DATE])

                PrimerInputElementType.EXPIRY_DATE ->
                    FieldFocuser.focus(cardInputFields[PrimerInputElementType.CVV])

                PrimerInputElementType.CVV -> {
                    if (primerViewModel.showCardInformation.value.isCardHolderNameEnabled()) {
                        takeFocusCardholderName()
                    } else {
                        binding.billingAddressForm.findNextFocus()
                    }
                }

                else -> Unit
            }
        }
    }

    private fun takeFocusCardholderName() = FieldFocuser.focus(cardInputFields[PrimerInputElementType.CARDHOLDER_NAME])

    private fun setValidationErrors() {
        val errors =
            cardViewModel.cardValidationErrors.value
                .plus(cardViewModel.billingAddressValidationErrors.value)

        binding.btnSubmitForm.isEnabled = cardViewModel.isValid()

        val showAll = cardViewModel.submitted

        cardInputFields.plus(binding.billingAddressForm.fieldsMap()).entries.forEach {
            val dirty = getIsDirty(it.key)
            val focused = it.value.isFocused

            if (showAll || dirty) {
                setValidationErrorState(
                    it.value,
                    if (focused) null else errors.find { err -> err.inputElementType == it.key },
                    it.key,
                )
            }
        }
    }

    private fun logValidationErrors() =
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var lastValidationErrors = emptySet<SyncValidationError>()
                cardViewModel.cardValidationErrors
                    .combine(
                        cardViewModel.billingAddressValidationErrors,
                    ) { cardValidationErrors, billingAddressValidationErrors ->
                        val validationErrors = cardValidationErrors.plus(billingAddressValidationErrors)
                        val validationErrorsDiff = validationErrors.minus(lastValidationErrors)
                        lastValidationErrors = validationErrors.toSet()
                        validationErrorsDiff.forEach { validationError ->
                            primerViewModel.addAnalyticsEvent(
                                MessageAnalyticsParams(
                                    messageType = MessageType.VALIDATION_FAILED,
                                    message = validationError.inputElementType.name,
                                    severity = Severity.WARN,
                                    context = ErrorContextParams(errorId = validationError.errorId),
                                ),
                            )
                        }
                    }.collect()
            }
        }

    private fun setValidationErrorState(
        input: TextInputWidget,
        error: SyncValidationError?,
        type: PrimerInputElementType,
    ) {
        val isEnableError = error != null
        input.isErrorEnabled = isEnableError
        if (!isLastInSection(input)) input.setMarginBottomForError(isEnableError)
        if (error == null) {
            input.error = error
        } else {
            requireContext()
                .let { context ->
                    input.error = error.errorFormatId?.let {
                        context.getString(error.errorFormatId, context.getString(error.fieldId))
                    } ?: error.errorResId?.let { context.getString(it) }
                    primerViewModel.addAnalyticsEvent(
                        MessageAnalyticsParams(
                            MessageType.VALIDATION_FAILED,
                            input.error.toString(),
                            Severity.INFO,
                        ),
                    )
                }
                .run {
                    if (type == PrimerInputElementType.CARD_NUMBER) {
                        val inputFrame = binding.cardFormCardNumber
                        inputFrame.suffixText = ""
                    }
                }
        }
    }

    /**
     * Method for handle fields when need to setup margin for correct space between fields
     * in error state. Error state, field with description of error.
     */
    private fun isLastInSection(input: TextInputWidget): Boolean {
        val cardHolderInputView = cardInputFields[PrimerInputElementType.CARDHOLDER_NAME]
        val isCvvField = cardInputFields[PrimerInputElementType.CVV] == input
        val isExpiryDateField = cardInputFields[PrimerInputElementType.EXPIRY_DATE] == input
        return ((isCvvField || isExpiryDateField) && cardHolderInputView?.isVisible == false) ||
            (cardHolderInputView == input && cardHolderInputView.isVisible)
    }

    private fun onKeyboardVisibilityChanged(visible: Boolean) {
        val hasFocus = cardInputFields.entries.any { it.value.isFocused }
        if (hasFocus && !visible) cardInputFields.entries.forEach { it.value.clearFocus() }
    }

    private fun getIsDirty(inputElementType: PrimerInputElementType): Boolean {
        return dirtyMap[inputElementType] ?: false
    }

    companion object {
        private const val ALPHA_HALF = 0.5f
        private const val ALPHA_VISIBLE = 1f

        @JvmStatic
        fun newInstance(): CardFormFragment {
            return CardFormFragment()
        }
    }
}
