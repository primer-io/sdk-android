package io.primer.android.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.SessionState
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.domain.payments.create.CreatePaymentInteractor
import io.primer.android.domain.payments.resume.ResumePaymentInteractor
import io.primer.android.data.configuration.models.CheckoutModuleType
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.models.PaymentMethodVaultTokenInternal
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.base.None
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsDeleteInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsExchangeInteractor
import io.primer.android.domain.payments.methods.VaultedPaymentMethodsInteractor
import io.primer.android.domain.payments.methods.models.VaultDeleteParams
import io.primer.android.domain.payments.methods.models.VaultTokenParams
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus
import io.primer.android.data.configuration.models.CountryCode
import io.primer.android.data.payments.methods.models.toPaymentMethodVaultToken
import io.primer.android.model.MonetaryAmount
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.methods.models.PaymentModuleParams
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.payment.PaymentMethodUiType
import io.primer.android.payment.SelectedPaymentMethodBehaviour
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.ui.AmountLabelContentFactory
import io.primer.android.ui.PaymentMethodButtonGroupFactory
import io.primer.android.utils.SurchargeFormatter
import java.util.Collections
import java.util.Currency
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
@ExperimentalCoroutinesApi
internal class PrimerViewModel(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodModulesInteractor: PaymentMethodModulesInteractor,
    private val vaultedPaymentMethodsInteractor: VaultedPaymentMethodsInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val exchangeInteractor: VaultedPaymentMethodsExchangeInteractor,
    private val vaultedPaymentMethodsDeleteInteractor: VaultedPaymentMethodsDeleteInteractor,
    private val createPaymentInteractor: CreatePaymentInteractor,
    private val resumePaymentInteractor: ResumePaymentInteractor,
    private val actionInteractor: ActionInteractor,
    private val config: PrimerConfig,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : BaseViewModel(analyticsInteractor), EventBus.EventListener {

    private lateinit var subscription: EventBus.SubscriptionHandle

    private val _selectedPaymentMethodId = MutableLiveData("")
    private val _transactionId = MutableLiveData("")

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

    val showPostalCode: LiveData<Boolean> = _state.asFlow()
        .flatMapLatest { configurationInteractor(ConfigurationParams(true)) }
        .map { configuration ->
            val module =
                configuration.checkoutModules.find { m ->
                    m.type == CheckoutModuleType.BILLING_ADDRESS
                }
            module?.options?.get("all") ?: module?.options?.get("postalCode") ?: false
        }
        .asLiveData()

    val showCardholderName: LiveData<Boolean> = _state.asFlow()
        .flatMapLatest { configurationInteractor(ConfigurationParams(true)) }
        .map { configuration ->
            val module =
                configuration.checkoutModules.find { m ->
                    m.type == CheckoutModuleType.CARD_INFORMATION
                }
            module?.options?.get("all") ?: module?.options?.get("cardHolderName") ?: true
        }
        .asLiveData()

    val orderCountry: LiveData<CountryCode?> = _state.asFlow()
        .flatMapLatest { configurationInteractor(ConfigurationParams(true)) }
        .map { configuration -> configuration.clientSession?.order?.countryCode }
        .asLiveData()

    private val postalCode = MutableLiveData<String?>()
    fun setPostalCode(data: String) = postalCode.postValue(data)

    fun goToVaultedPaymentMethodsView() {
        logGoToVaultedPaymentMethodsView()
        viewStatus.postValue(ViewStatus.VIEW_VAULTED_PAYMENT_METHODS)
    }

    fun goToSelectPaymentMethodsView() {
        logGoToSelectPaymentMethodsView()
        reselectSavedPaymentMethod()
        viewStatus.postValue(ViewStatus.SELECT_PAYMENT_METHOD)
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

    fun fetchConfiguration() {
        viewModelScope.launch {
            configurationInteractor(ConfigurationParams(false)).flatMapLatest {
                paymentMethodModulesInteractor(PaymentModuleParams(true)).zip(
                    vaultedPaymentMethodsInteractor(None())
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
                        _selectedPaymentMethod.postValue(paymentMethod)
                        paymentMethod.behaviours.firstOrNull()
                            ?.let {
                                val isNotForm = paymentMethod.type != PaymentMethodUiType.FORM
                                if (isNotForm) {
                                    executeBehaviour(it)
                                }
                            }
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
        completion: ((Error?) -> Unit) = {},
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
        val type = token.surchargeType
        val network = token.paymentInstrumentData?.binData?.network
        dispatchAction(
            ActionUpdateSelectPaymentMethodParams(
                PaymentMethodType.safeValueOf(type),
                network
            )
        )
    }

    fun exchangePaymentMethodToken(token: PaymentMethodVaultTokenInternal) = viewModelScope.launch {
        exchangeInteractor(VaultTokenParams(token)).collect { }
    }

    fun deleteToken(token: BasePaymentToken) = viewModelScope.launch {
        vaultedPaymentMethodsDeleteInteractor(VaultDeleteParams(token.token)).collect { token ->
            _vaultedPaymentMethods.value =
                vaultedPaymentMethods.value?.filter { it.token != token }
        }
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
        get() = SurchargeFormatter(actionInteractor.surcharges, currency)

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

    fun amountLabelMonetaryAmount(
        config: PrimerConfig
    ): MonetaryAmount? = AmountLabelContentFactory.build(config, savedPaymentMethodSurcharge)

    val monetaryAmount: MonetaryAmount? get() = config.monetaryAmount

    fun findSurchargeAmount(
        type: String,
        network: String? = null
    ): Int = formatter.getSurchargeForPaymentMethodType(type, network)

    fun setSelectedPaymentMethodId(id: String) {
        _selectedPaymentMethodId.value = id
    }

    fun getSelectedPaymentMethodId(): String = _selectedPaymentMethodId.value.orEmpty()

    fun emitPostalCode(completion: (() -> Unit) = {}) {
        val postalCodeValue: String = postalCode.value ?: return completion()
        if (showPostalCode.value != true) return completion()

        val currentCustomer = config.settings.customer
        val currentBillingAddress = currentCustomer.billingAddress

        val action = ActionUpdateBillingAddressParams(
            currentCustomer.firstName,
            currentCustomer.lastName,
            currentBillingAddress?.addressLine1,
            currentBillingAddress?.addressLine2,
            currentBillingAddress?.city,
            postalCodeValue,
            currentBillingAddress?.countryCode?.name,
        )

        dispatchAction(action) { error ->
            if (error == null) completion()
            else setState(SessionState.ERROR)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(NonCancellable) {
            analyticsInteractor.send().collect { }
        }
        subscription.unregister()
    }

    override fun onEvent(e: CheckoutEvent) {
        when (e) {
            is CheckoutEvent.TokenAddedToVaultInternal -> {
                val hasMatch = vaultedPaymentMethods.value
                    ?.count { it.token == e.data.token } ?: 0 > 0
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

    private fun logSelectPaymentMethod(paymentMethodType: PaymentMethodType) {
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
}
