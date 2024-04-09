package io.primer.android.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.SessionState
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.domain.payments.vault.model.card.PrimerVaultedCardAdditionalData
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.data.payments.methods.models.toPaymentMethodVaultToken
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.models.PrimerCountry
import io.primer.android.domain.action.models.PrimerPhoneCode
import io.primer.android.domain.base.None
import io.primer.android.domain.currencyformat.interactors.FetchCurrencyFormatDataInteractor
import io.primer.android.domain.currencyformat.interactors.FormatAmountToCurrencyInteractor
import io.primer.android.domain.currencyformat.models.FormatCurrencyParams
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.models.VaultInstrumentParams
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.model.MonetaryAmount
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.payment.billing.BillingAddressValidator
import io.primer.android.payment.config.toImageDisplayMetadata
import io.primer.android.payment.config.toTextDisplayMetadata
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.ui.AmountLabelContentFactory
import io.primer.android.ui.PaymentMethodButtonGroupFactory
import io.primer.android.utils.SurchargeFormatter
import io.primer.android.utils.orNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.Currency

@ExperimentalCoroutinesApi
@Suppress("LongParameterList", "TooManyFunctions")
internal class PrimerViewModel(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val vaultedPaymentMethodsInteractor: VaultedPaymentMethodsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val exchangeInteractor: VaultedPaymentMethodsExchangeInteractor,
    private val vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
    private val actionInteractor: ActionInteractor,
    private val fetchCurrencyFormatDataInteractor: FetchCurrencyFormatDataInteractor,
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val config: PrimerConfig,
    private val billingAddressValidator: BillingAddressValidator,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : BaseViewModel(analyticsInteractor), EventBus.EventListener {

    private lateinit var subscription: EventBus.SubscriptionHandle

    private val _selectedPaymentMethodId = MutableLiveData("")
    private val _transactionId = MutableLiveData("")

    private val _selectedCountryCode = MutableLiveData<PrimerCountry?>()
    val selectCountryCode: LiveData<PrimerCountry?> = _selectedCountryCode

    private val _selectedPhoneCode = MutableLiveData<PrimerPhoneCode?>()
    val selectPhoneCode: LiveData<PrimerPhoneCode?> = _selectedPhoneCode

    private val _keyboardVisible = MutableLiveData(false)
    val keyboardVisible: LiveData<Boolean> = _keyboardVisible

    val viewStatus: MutableLiveData<ViewStatus> =
        MutableLiveData<ViewStatus>(ViewStatus.INITIALIZING)

    private val _state = MutableLiveData(SessionState.AWAITING_USER)
    val state: LiveData<SessionState> = _state

    fun setState(s: SessionState) {
        _state.postValue(s)
    }

    val selectedSavedPaymentMethod: PaymentMethodVaultTokenInternal?
        get() = vaultedPaymentMethods.value?.find { it.token == _selectedPaymentMethodId.value }

    val shouldDisplaySavedPaymentMethod: Boolean
        get() = config.intent.paymentMethodIntent.isNotVault && selectedSavedPaymentMethod != null

    private val _vaultedPaymentMethods = MutableLiveData<List<PaymentMethodVaultTokenInternal>>(
        Collections.emptyList()
    )
    val vaultedPaymentMethods: LiveData<List<PaymentMethodVaultTokenInternal>> =
        _vaultedPaymentMethods

    private val _paymentMethods = MutableLiveData<List<PaymentMethodDescriptor>>(emptyList())
    val paymentMethods: LiveData<List<PaymentMethodDescriptor>> = _paymentMethods

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethodDescriptor?>(null)
    val selectedPaymentMethod: LiveData<PaymentMethodDescriptor?> = _selectedPaymentMethod

    private val _selectedPaymentMethodBehaviour =
        MutableLiveData<SelectedPaymentMethodBehaviour?>(null)
    val selectedPaymentMethodBehaviour: LiveData<SelectedPaymentMethodBehaviour?> =
        _selectedPaymentMethodBehaviour

    private val _checkoutEvent = MutableLiveData<CheckoutEvent>()
    val checkoutEvent: LiveData<CheckoutEvent> = _checkoutEvent

    private val _navigateActionEvent = MutableLiveData<SelectedPaymentMethodBehaviour>()
    val navigateActionEvent: LiveData<SelectedPaymentMethodBehaviour> = _navigateActionEvent

    val billingAddressFields = MutableLiveData(mutableMapOf<PrimerInputElementType, String?>())

    val showBillingFields: LiveData<Map<String, Boolean>?> by lazy {
        configurationInteractor(ConfigurationParams(true))
            .map { configuration ->
                val module =
                    configuration.checkoutModules.find { m ->
                        m.type == CheckoutModuleType.BILLING_ADDRESS
                    }
                module?.options
            }
            .asLiveData()
    }

    val showCardInformation: LiveData<Map<String, Boolean>?> = _state.asFlow()
        .flatMapLatest { configurationInteractor(ConfigurationParams(true)) }
        .map { configuration ->
            val module =
                configuration.checkoutModules.find { m ->
                    m.type == CheckoutModuleType.CARD_INFORMATION
                }
            module?.options
        }
        .asLiveData()

    suspend fun shouldShowCaptureCvv(): Boolean =
        configurationInteractor(ConfigurationParams(true)).map { configuration ->
            configuration.paymentMethods.find { paymentMethod ->
                paymentMethod.type == PaymentMethodType.PAYMENT_CARD.name
            }?.options?.captureVaultedCardCvv
        }.last() == true && selectedSavedPaymentMethod?.paymentMethodType ==
            PaymentMethodType.PAYMENT_CARD.name

    fun goToVaultedPaymentMethodsView() {
        logGoToVaultedPaymentMethodsView()
        viewStatus.postValue(ViewStatus.VIEW_VAULTED_PAYMENT_METHODS)
    }

    fun goToSelectPaymentMethodsView() {
        logGoToSelectPaymentMethodsView()
        reselectSavedPaymentMethod()
        viewStatus.postValue(ViewStatus.SELECT_PAYMENT_METHOD)
    }

    fun goToVaultedPaymentCvvRecaptureView() {
        viewStatus.postValue(ViewStatus.VAULTED_PAYMENT_RECAPTURE_CVV)
    }

    fun selectPaymentMethod(paymentMethodDescriptor: PaymentMethodDescriptor) {
        logSelectPaymentMethod(paymentMethodDescriptor.config.type)
        _selectedPaymentMethod.value = paymentMethodDescriptor
    }

    fun setCheckoutEvent(event: CheckoutEvent) {
        _checkoutEvent.postValue(event)
    }

    fun setKeyboardVisibility(visible: Boolean) {
        _keyboardVisible.postValue(visible)
    }

    fun executeBehaviour(behaviour: SelectedPaymentMethodBehaviour) {
        _selectedPaymentMethodBehaviour.value = behaviour
    }

    fun navigateTo(behaviour: SelectedPaymentMethodBehaviour) {
        _navigateActionEvent.postValue(behaviour)
    }

    fun fetchConfiguration() {
        viewModelScope.launch {
            configurationInteractor(ConfigurationParams(config.settings.fromHUC))
                .flatMapLatest {
                    flowOf(fetchCurrencyFormatDataInteractor(None())).zip(
                        paymentMethodModulesInteractor(None())
                    ) { _, descriptorsHolder ->
                        descriptorsHolder
                    }.zip(
                        vaultedPaymentMethodsInteractor(
                            VaultInstrumentParams(config.settings.fromHUC.not())
                        )
                    ) { descriptorsHolder, paymentModelTokens ->
                        _vaultedPaymentMethods.postValue(paymentModelTokens)
                        if (getSelectedPaymentMethodId().isEmpty() &&
                            paymentModelTokens.isNotEmpty() &&
                            descriptorsHolder.selectedPaymentMethodDescriptor == null
                        ) {
                            setSelectedPaymentMethodId(paymentModelTokens.first().token)
                        }

                        _paymentMethods.postValue(descriptorsHolder.descriptors)
                        descriptorsHolder.selectedPaymentMethodDescriptor?.let {
                            val paymentMethod = descriptorsHolder.selectedPaymentMethodDescriptor
                            paymentMethod.behaviours.firstOrNull()
                                ?.let {
                                    val isNotForm = paymentMethod.type != PaymentMethodUiType.FORM
                                    if (isNotForm) {
                                        executeBehaviour(it)
                                    }
                                } ?: run {
                                // ignore and let the `selectedBehaviour` execute
                            }
                            _selectedPaymentMethod.postValue(paymentMethod)
                        } ?: run {
                            viewStatus.postValue(ViewStatus.SELECT_PAYMENT_METHOD)
                        }
                    }
                }.collect { }
        }

        subscription = EventBus.subscribe(this)
    }

    fun dispatchAction(
        actionUpdateParams: BaseActionUpdateParams,
        resetState: Boolean = true,
        completion: ((Error?) -> Unit) = {}
    ) {
        viewModelScope.launch {
            actionInteractor(actionUpdateParams)
                .onStart {
                    setState(SessionState.AWAITING_APP)
                }
                .catch {
                    setState(SessionState.ERROR)
                    completion(Error(it))
                }
                .collect {
                    if (resetState) setState(SessionState.AWAITING_USER)
                    completion(null)
                }
        }
    }

    fun reselectSavedPaymentMethod() {
        val list = vaultedPaymentMethods.value ?: return
        val token = list.find { p -> p.token == getSelectedPaymentMethodId() } ?: return
        val network = token.paymentInstrumentData?.binData?.network
        dispatchAction(
            ActionUpdateSelectPaymentMethodParams(
                token.paymentMethodType ?: return,
                network
            )
        )
    }

    fun getPaymentMethodsDisplayMetadata() = paymentMethodsImplementationInteractor.execute(None())
        .map {
            val isDarkMode = config.settings.uiOptions.theme.isDarkMode == true
            when (it.buttonMetadata?.text.isNullOrBlank()) {
                true -> it.toImageDisplayMetadata(isDarkMode)
                false -> it.toTextDisplayMetadata(isDarkMode)
            }
        }

    fun exchangePaymentMethodToken(token: PaymentMethodVaultTokenInternal) = viewModelScope.launch {
        exchangeInteractor(VaultTokenParams(token.token, token.paymentMethodType)).collect { }
    }

    fun exchangePaymentMethodTokenWithAdditionalData(
        token: PaymentMethodVaultTokenInternal,
        additionalData: String
    ) = viewModelScope.launch {
        exchangeInteractor(
            VaultTokenParams(
                token.token,
                token.paymentMethodType,
                (PrimerVaultedCardAdditionalData(additionalData))
            )
        ).collect { }
    }

    fun deleteToken(token: BasePaymentToken) = viewModelScope.launch {
        vaultedPaymentMethodsDeleteInteractor(VaultDeleteParams(token.token)).onSuccess { token ->
            _vaultedPaymentMethods.value =
                vaultedPaymentMethods.value?.filter { it.token != token }
        }
            .onFailure { }
    }

    fun initializeAnalytics() = viewModelScope.launch {
        analyticsInteractor.initialize().collect { }
    }

    val paymentMethodButtonGroupFactory: PaymentMethodButtonGroupFactory
        get() = PaymentMethodButtonGroupFactory(actionInteractor.surcharges, formatter)

    private val currency: Currency
        get() = Currency.getInstance(config.monetaryAmount?.currency)

    val surchargeDisabled: Boolean
        get() {
            if (config.intent.paymentMethodIntent.isVault) return true
            if (actionInteractor.surchargeDataEmptyOrZero) return true
            return false
        }

    private val formatter: SurchargeFormatter
        get() = SurchargeFormatter(actionInteractor, amountToCurrencyInteractor, currency)

    private val token: BasePaymentToken?
        get() = _selectedPaymentMethodId.value
            ?.let { id -> _vaultedPaymentMethods.value?.find { it.token == id } }

    private val savedPaymentMethodSurcharge: Int
        get() = formatter.getSurchargeForSavedPaymentMethod(token)

    fun savedPaymentMethodSurchargeLabel(context: Context): String = formatter
        .getSurchargeLabelTextForPaymentMethodType(savedPaymentMethodSurcharge, context)

    fun amountLabelMonetaryAmount(
        paymentMethodType: String
    ): MonetaryAmount? = AmountLabelContentFactory.build(
        config,
        formatter.getSurchargeForPaymentMethodType(paymentMethodType)
    )

    fun amountLabel(
        config: PrimerConfig
    ): String? = amountToCurrencyString(
        AmountLabelContentFactory.build(
            config,
            savedPaymentMethodSurcharge
        )
    )

    private val monetaryAmount: MonetaryAmount? get() = config.monetaryAmount

    fun findSurchargeAmount(
        type: String,
        network: String? = null
    ): Int = formatter.getSurchargeForPaymentMethodType(type, network)

    fun setSelectedPaymentMethodId(id: String) {
        _selectedPaymentMethodId.value = id
    }

    fun getSelectedPaymentMethodId(): String = _selectedPaymentMethodId.value.orEmpty()

    fun emitBillingAddress(completion: ((error: String?) -> Unit) = {}) {
        val enabledBillingAddressFields = showBillingFields.value
        if (enabledBillingAddressFields == null ||
            enabledBillingAddressFields.isEmpty() ||
            enabledBillingAddressFields.values.firstOrNull { it } == null
        ) {
            completion(null)
            return
        }
        val billingAddressFields = this.billingAddressFields.value ?: run {
            completion(null)
            return
        }
        val countryCode = billingAddressFields[PrimerInputElementType.COUNTRY_CODE]
        val firstName = billingAddressFields[PrimerInputElementType.FIRST_NAME].orNull()
        val lastName = billingAddressFields[PrimerInputElementType.LAST_NAME].orNull()
        val addressLine1 = billingAddressFields[PrimerInputElementType.ADDRESS_LINE_1].orNull()
        val addressLine2 = billingAddressFields[PrimerInputElementType.ADDRESS_LINE_2].orNull()
        val postalCode = billingAddressFields[PrimerInputElementType.POSTAL_CODE].orNull()
        val city = billingAddressFields[PrimerInputElementType.CITY].orNull()
        val state = billingAddressFields[PrimerInputElementType.STATE].orNull()

        val action = ActionUpdateBillingAddressParams(
            firstName,
            lastName,
            addressLine1,
            addressLine2,
            city,
            postalCode,
            countryCode,
            state
        )

        dispatchAction(action) { error ->
            completion(error?.message)
            if (error != null) setState(SessionState.ERROR)
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingAddressFields.value?.clear()
        _selectedCountryCode.postValue(null)
        viewModelScope.launch(NonCancellable) {
            analyticsInteractor.send().collect { }
        }
        subscription.unregister()
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenAddedToVaultInternal -> {
                val hasMatch = (
                    vaultedPaymentMethods.value
                        ?.count { it.token == e.data.token } ?: 0
                    ) > 0
                if (hasMatch) {
                    _vaultedPaymentMethods.value = vaultedPaymentMethods.value?.plus(
                        e.data.toPaymentMethodVaultToken()
                    )
                }
            }

            is CheckoutEvent.PaymentContinue -> {
                viewModelScope.launch {
                    createPaymentInteractor(
                        CreatePaymentParams(e.data.token, e.resumeHandler)
                    ).collect {
                        _transactionId.postValue(it)
                    }
                }
            }

            is CheckoutEvent.ResumeSuccessInternal -> {
                viewModelScope.launch {
                    resumePaymentInteractor(
                        ResumeParams(
                            _transactionId.value.orEmpty(),
                            e.resumeToken,
                            e.resumeHandler
                        )
                    ).collect { }
                }
            }

            else -> Unit
        }
    }

    private fun logGoToVaultedPaymentMethodsView() {
        addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                config.toPlace(),
                ObjectId.MANAGE
            )
        )
    }

    private fun logGoToSelectPaymentMethodsView() {
        addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                Place.PAYMENT_METHODS_LIST,
                ObjectId.BACK
            )
        )
    }

    private fun logSelectPaymentMethod(paymentMethodType: String) {
        addAnalyticsEvent(
            UIAnalyticsParams(
                AnalyticsAction.CLICK,
                ObjectType.BUTTON,
                config.toPlace(),
                ObjectId.SELECT,
                PaymentMethodContextParams(paymentMethodType)
            )
        )
    }

    fun setSelectedCountry(country: PrimerCountry) {
        _selectedCountryCode.value = country
    }

    fun clearSelectedCountry() {
        _selectedCountryCode.postValue(null)
    }

    fun setSelectedPhoneCode(phoneCode: PrimerPhoneCode) {
        _selectedPhoneCode.postValue(phoneCode)
    }

    fun validateBillingAddress(): List<SyncValidationError> = billingAddressValidator.validate(
        billingAddressFields.value.orEmpty(),
        showBillingFields.value.orEmpty()
    )

    fun amountToCurrencyString(amount: MonetaryAmount?) =
        amount?.let { amountToCurrencyInteractor.execute(params = FormatCurrencyParams(it)) }

    fun amountToCurrencyString() =
        monetaryAmount?.let { amountToCurrencyString(it) }
}
