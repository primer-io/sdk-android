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
import io.primer.android.analytics.data.models.DropInSourceAnalyticsContext
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.analytics.data.models.TimerId
import io.primer.android.analytics.data.models.TimerType
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.PaymentMethodContextParams
import io.primer.android.analytics.domain.models.TimerAnalyticsParams
import io.primer.android.analytics.domain.models.UIAnalyticsParams
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.components.assets.displayMetadata.PaymentMethodsImplementationInteractor
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManagerInterface
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManager
import io.primer.android.components.manager.vault.PrimerHeadlessUniversalCheckoutVaultManagerInterface
import io.primer.android.configuration.domain.BasicOrderInfoInteractor
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.model.findFirstInstance
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.domain.None
import io.primer.android.currencyformat.domain.FormatAmountToCurrencyInteractor
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.errors.domain.ErrorMapperRegistry
import io.primer.android.model.SyncValidationError
import io.primer.android.payment.billing.BillingAddressValidator
import io.primer.android.payment.config.toImageDisplayMetadata
import io.primer.android.payment.config.toTextDisplayMetadata
import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.paymentMethods.PaymentMethodUiType
import io.primer.android.paymentMethods.core.PaymentMethodMapping
import io.primer.android.paymentMethods.core.PrimerHeadlessSdkInitInteractor
import io.primer.android.paymentMethods.core.domain.PrimerEventsInteractor
import io.primer.android.paymentMethods.core.domain.events.PrimerEvent
import io.primer.android.paymentMethods.core.domain.model.HeadlessSdkInitParams
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.PrimerDropInPaymentMethodDescriptorRegistry
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.presentation.base.BaseViewModel
import io.primer.android.surcharge.domain.SurchargeInteractor
import io.primer.android.surcharge.utils.SurchargeFormatter
import io.primer.android.ui.PaymentMethodButtonGroupFactory
import io.primer.android.utils.orNull
import io.primer.android.vault.implementation.vaultedMethods.domain.PrimerVaultedPaymentMethodAdditionalData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.Currency
import kotlin.time.TimeSource

@ExperimentalCoroutinesApi
@Suppress("LongParameterList", "TooManyFunctions")
internal class PrimerViewModel(
    private val configurationInteractor: ConfigurationInteractor,
    private val paymentMethodsImplementationInteractor: PaymentMethodsImplementationInteractor,
    private val analyticsInteractor: AnalyticsInteractor,
    private val headlessSdkInitInteractor: PrimerHeadlessSdkInitInteractor,
    private val eventsInteractor: PrimerEventsInteractor,
    private val actionInteractor: ActionInteractor,
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val config: PrimerConfig,
    private val billingAddressValidator: BillingAddressValidator,
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor,
    private val surchargeInteractor: SurchargeInteractor,
    private val registry: PrimerDropInPaymentMethodDescriptorRegistry,
    private val paymentMethodMapping: PaymentMethodMapping,
    private val errorMapperRegistry: ErrorMapperRegistry,
    private val checkoutErrorHandler: CheckoutErrorHandler,
    private val pollingStartHandler: PollingStartHandler,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : BaseViewModel(analyticsInteractor), DISdkComponent {

    private val vaultManager: PrimerHeadlessUniversalCheckoutVaultManagerInterface by lazy {
        PrimerHeadlessUniversalCheckoutVaultManager.newInstance()
    }

    private val _selectedPaymentMethodId = MutableLiveData("")

    private val _selectedCountryCode = MutableLiveData<PrimerCountry?>()
    val selectCountryCode: LiveData<PrimerCountry?> = _selectedCountryCode

    private val _selectedPhoneCode = MutableLiveData<PrimerPhoneCode?>()
    val selectPhoneCode: LiveData<PrimerPhoneCode?> = _selectedPhoneCode

    private val _keyboardVisible = MutableLiveData(false)
    val keyboardVisible: LiveData<Boolean> = _keyboardVisible

    private val _viewStatus: MutableLiveData<ViewStatus> =
        MutableLiveData<ViewStatus>(ViewStatus.Initializing)
    val viewStatus: LiveData<ViewStatus> = _viewStatus

    private var _selectedPaymentMethodNativeUiManager: PrimerHeadlessUniversalCheckoutNativeUiManagerInterface? = null

    init {
        viewModelScope.launch {
            pollingStartHandler.startPolling.collectLatest {
                setViewStatus(ViewStatus.PollingStarted(it.statusUrl, it.paymentMethodType))
            }
        }
    }

    fun setViewStatus(viewStatus: ViewStatus) {
        _viewStatus.postValue(viewStatus)
    }

    private val _state = MutableLiveData(SessionState.AWAITING_USER)
    val state: LiveData<SessionState> = _state

    fun setState(s: SessionState) {
        _state.postValue(s)
    }

    val selectedSavedPaymentMethod: PrimerVaultedPaymentMethod?
        get() = vaultedPaymentMethods.value?.find { it.id == _selectedPaymentMethodId.value }

    val shouldDisplaySavedPaymentMethod: Boolean
        get() = config.intent.paymentMethodIntent.isNotVault && selectedSavedPaymentMethod != null

    private val _vaultedPaymentMethods = MutableLiveData<List<PrimerVaultedPaymentMethod>>(
        Collections.emptyList()
    )
    val vaultedPaymentMethods: LiveData<List<PrimerVaultedPaymentMethod>> =
        _vaultedPaymentMethods

    private val _paymentMethods = MutableLiveData<List<PaymentMethodDropInDescriptor>>(emptyList())
    val paymentMethods: LiveData<List<PaymentMethodDropInDescriptor>> = _paymentMethods

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethodDropInDescriptor?>(null)
    val selectedPaymentMethod: LiveData<PaymentMethodDropInDescriptor?> = _selectedPaymentMethod

    private val _paymentMethodBehaviour =
        MutableLiveData<PaymentMethodBehaviour?>(null)
    val paymentMethodBehaviour: LiveData<PaymentMethodBehaviour?> =
        _paymentMethodBehaviour

    val selectedSavedPaymentMethodDescriptor: PaymentMethodDropInDescriptor?
        get() {
            val type = selectedSavedPaymentMethod?.paymentMethodType ?: return null
            return paymentMethods.value.orEmpty().firstOrNull { it.paymentMethodType == type }
        }

    private val _navigateActionEvent = MutableLiveData<PaymentMethodBehaviour>()
    val navigateActionEvent: LiveData<PaymentMethodBehaviour> = _navigateActionEvent

    val billingAddressFields = MutableLiveData(mutableMapOf<PrimerInputElementType, String?>())

    val showBillingFields: LiveData<Map<String, Boolean>?> by lazy {
        flow { emit(configurationInteractor(ConfigurationParams(CachePolicy.ForceCache)).getOrThrow()) }
            .map { configuration ->
                val billingAddress = configuration.checkoutModules.findFirstInstance<CheckoutModule.BillingAddress>()
                billingAddress?.options
            }
            .asLiveData()
    }

    val showCardInformation: LiveData<CheckoutModule.CardInformation?> = _state.asFlow()
        .map { configurationInteractor(ConfigurationParams(CachePolicy.ForceCache)).getOrThrow() }
        .map { configuration ->
            configuration.checkoutModules.findFirstInstance<CheckoutModule.CardInformation>()
        }
        .asLiveData()

    suspend fun shouldShowCaptureCvv(): Boolean =
        configurationInteractor(ConfigurationParams(CachePolicy.ForceCache)).map { configuration ->
            configuration.paymentMethods.find { paymentMethod ->
                paymentMethod.type == PaymentMethodType.PAYMENT_CARD.name
            }?.options?.captureVaultedCardCvv
        }.getOrNull() == true && selectedSavedPaymentMethod?.paymentMethodType ==
            PaymentMethodType.PAYMENT_CARD.name

    fun goToVaultedPaymentMethodsView() {
        logGoToVaultedPaymentMethodsView()
        _viewStatus.postValue(ViewStatus.ViewVaultedPaymentMethods)
    }

    fun goToSelectPaymentMethodsView() {
        logGoToSelectPaymentMethodsView()
        reselectSavedPaymentMethod()
        _viewStatus.postValue(ViewStatus.SelectPaymentMethod)
    }

    fun goToVaultedPaymentCvvRecaptureView() {
        _viewStatus.postValue(ViewStatus.VaultedPaymentRecaptureCvv)
    }

    fun selectPaymentMethod(paymentMethodDescriptor: PaymentMethodDropInDescriptor?) {
        paymentMethodDescriptor?.let { logSelectPaymentMethod(it.paymentMethodType) }
        _selectedPaymentMethod.value = paymentMethodDescriptor
    }

    fun setSelectedPaymentMethodNativeUiManager(
        nativeUiManager: PrimerHeadlessUniversalCheckoutNativeUiManagerInterface
    ) {
        addCloseable {
            _selectedPaymentMethodNativeUiManager = null
            nativeUiManager.cleanup()
        }
        _selectedPaymentMethodNativeUiManager = nativeUiManager
    }

    fun clearSelectedPaymentMethodNativeUiManager() {
        _selectedPaymentMethodNativeUiManager?.cleanup()
        _selectedPaymentMethodNativeUiManager = null
    }

    fun setKeyboardVisibility(visible: Boolean) {
        _keyboardVisible.postValue(visible)
    }

    fun executeBehaviour(behaviour: PaymentMethodBehaviour) {
        _paymentMethodBehaviour.value = behaviour
    }

    fun navigateTo(behaviour: PaymentMethodBehaviour) {
        _navigateActionEvent.postValue(behaviour)
    }

    fun fetchConfiguration() {
        viewModelScope.launch {
            launch {
                val timeSource = TimeSource.Monotonic
                val start = timeSource.markNow()
                eventsInteractor.execute(None).collectLatest { event: PrimerEvent ->
                    when (event) {
                        is PrimerEvent.AvailablePaymentMethodsLoaded -> {
                            vaultManager.fetchVaultedPaymentMethods()
                                .fold(
                                    { paymentModelTokens ->
                                        _vaultedPaymentMethods.postValue(paymentModelTokens)

                                        val paymentMethodsHolder = event.paymentMethodsHolder

                                        if (getSelectedPaymentMethodId().isEmpty() &&
                                            paymentModelTokens.isNotEmpty() &&
                                            paymentMethodsHolder.selectedPaymentMethod == null
                                        ) {
                                            setSelectedPaymentMethodId(paymentModelTokens.first().id)
                                        }

                                        val descriptors =
                                            paymentMethodsHolder.paymentMethods.mapNotNull { paymentMethod ->
                                                runCatching {
                                                    paymentMethodMapping.getPaymentMethodDescriptorFor(
                                                        paymentMethodType = paymentMethod.paymentMethodType,
                                                        paymentMethodName = paymentMethod.paymentMethodName,
                                                        paymentMethodManagerCategory =
                                                        paymentMethod.paymentMethodManagerCategories.first()
                                                    )
                                                }
                                                    .onFailure {
                                                        checkoutErrorHandler.handle(
                                                            error = errorMapperRegistry.getPrimerError(it),
                                                            payment = null
                                                        )
                                                    }
                                                    .getOrNull()
                                                    ?.also { descriptor ->
                                                        registry.register(descriptor.paymentMethodType, descriptor)
                                                    }
                                            }

                                        _paymentMethods.postValue(descriptors)

                                        paymentMethodsHolder.selectedPaymentMethod?.let {
                                            val paymentMethod =
                                                descriptors.first {
                                                    it.paymentMethodType ==
                                                        paymentMethodsHolder.selectedPaymentMethod.paymentMethodType
                                                }
                                            paymentMethod.behaviours.firstOrNull()
                                                ?.let {
                                                    val isNotForm = paymentMethod.uiType != PaymentMethodUiType.FORM
                                                    if (isNotForm) {
                                                        executeBehaviour(it)
                                                    }
                                                } ?: run {
                                                // ignore and let the `selectedBehaviour` execute
                                            }
                                            _selectedPaymentMethod.postValue(paymentMethod)
                                        } ?: run {
                                            _viewStatus.postValue(ViewStatus.SelectPaymentMethod)
                                        }
                                    },
                                    {
                                        checkoutErrorHandler.handle(
                                            error = errorMapperRegistry.getPrimerError(it),
                                            payment = null
                                        )
                                    }
                                )
                            TimerAnalyticsParams(
                                id = TimerId.DROP_IN_LOADING,
                                timerType = TimerType.END,
                                duration = (timeSource.markNow() - start).inWholeMilliseconds,
                                context = DropInSourceAnalyticsContext(
                                    source = config.toDropInSource()
                                )
                            )
                        }

                        is PrimerEvent.CheckoutCompleted -> _viewStatus.postValue(
                            ViewStatus.ShowSuccess(
                                successType = event.successType,
                                checkoutAdditionalInfo = event.checkoutData?.additionalInfo
                            )
                        )

                        is PrimerEvent.CheckoutFailed -> _viewStatus.postValue(
                            ViewStatus.ShowError(
                                errorType = event.errorType,
                                message = event.errorMessage
                            )
                        )

                        PrimerEvent.Dismiss -> _viewStatus.postValue(ViewStatus.Dismiss)
                    }
                }
            }
            launch {
                headlessSdkInitInteractor.execute(
                    HeadlessSdkInitParams(clientToken = config.clientTokenBase64.orEmpty())
                )
                addAnalyticsEvent(
                    TimerAnalyticsParams(
                        id = TimerId.DROP_IN_LOADING,
                        timerType = TimerType.START,
                        context = DropInSourceAnalyticsContext(
                            source = config.toDropInSource()
                        )
                    )
                )
            }
        }
    }

    fun dispatchAction(
        actionUpdateParams: BaseActionUpdateParams,
        resetState: Boolean = true,
        completion: ((Error?) -> Unit) = {}
    ) {
        viewModelScope.launch {
            setState(SessionState.AWAITING_APP)
            actionInteractor(MultipleActionUpdateParams(listOf(actionUpdateParams)))
                .onFailure {
                    setState(SessionState.ERROR)
                    completion(Error(it))
                }
                .onSuccess {
                    if (resetState) setState(SessionState.AWAITING_USER)
                    completion(null)
                }
        }
    }

    fun reselectSavedPaymentMethod() {
        val list = vaultedPaymentMethods.value ?: return
        val token = list.find { p -> p.id == getSelectedPaymentMethodId() } ?: return
        val network = token.paymentInstrumentData.binData?.network
        dispatchAction(
            ActionUpdateSelectPaymentMethodParams(
                token.paymentMethodType,
                network
            )
        )
    }

    fun getPaymentMethodsDisplayMetadata(context: Context) = paymentMethodsImplementationInteractor.execute(None)
        .map {
            val isDarkMode = config.settings.uiOptions.theme.isDarkMode == true
            when (it.buttonMetadata?.text.isNullOrBlank()) {
                true -> it.toImageDisplayMetadata(isDarkMode)
                false -> it.toTextDisplayMetadata(isDarkMode, context)
            }
        }

    val paymentMethodButtonGroupFactory: PaymentMethodButtonGroupFactory
        get() = PaymentMethodButtonGroupFactory(surcharges = surchargeInteractor(None), formatter = formatter)

    val surchargeDisabled: Boolean
        get() {
            if (config.intent.paymentMethodIntent.isVault) return true
            if (
                surchargeInteractor(None).let { surcharges ->
                    surcharges.all { item -> item.value == 0 } || surcharges.isEmpty()
                }
            ) {
                return true
            }
            return false
        }

    private val formatter: SurchargeFormatter
        get() = SurchargeFormatter(
            surchargeInteractor = surchargeInteractor,
            amountToCurrencyInteractor = amountToCurrencyInteractor,
            currency = Currency.getInstance(basicOrderInfoInteractor(None).currencyCode)
        )

    private val token: PrimerVaultedPaymentMethod?
        get() = _selectedPaymentMethodId.value
            ?.let { id -> _vaultedPaymentMethods.value?.find { it.id == id } }

    private val savedPaymentMethodSurcharge: Int
        get() = formatter.getSurchargeForSavedPaymentMethod(token)

    fun savedPaymentMethodSurchargeLabel(context: Context): String = formatter
        .getSurchargeLabelTextForPaymentMethodType(savedPaymentMethodSurcharge, context)

    fun amountLabel(): String = getTotalAmountFormatted()

    fun findSurchargeAmount(
        type: String,
        network: String? = null
    ): Int = formatter.getSurchargeForPaymentMethodType(type, network)

    fun setSelectedPaymentMethodId(id: String) {
        _selectedPaymentMethod.value = null
        _selectedPaymentMethodId.value = id
    }

    fun getSelectedPaymentMethodId(): String = _selectedPaymentMethodId.value.orEmpty()

    fun emitBillingAddress(completion: ((error: String?) -> Unit) = {}) {
        val enabledBillingAddressFields = showBillingFields.value
        if (enabledBillingAddressFields.isNullOrEmpty() ||
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
        _selectedCountryCode.value = null
    }

    fun setSelectedPhoneCode(phoneCode: PrimerPhoneCode) {
        _selectedPhoneCode.postValue(phoneCode)
    }

    fun validateBillingAddress(): List<SyncValidationError> = billingAddressValidator.validate(
        billingAddressFields.value.orEmpty(),
        showBillingFields.value.orEmpty()
    )

    fun getTotalAmountFormatted(): String = basicOrderInfoInteractor.execute(None).let { orderInfo ->
        orderInfo.let {
            amountToCurrencyInteractor.execute(
                params = FormatCurrencyParams(
                    requireNotNull(
                        MonetaryAmount.create(
                            orderInfo.currencyCode,
                            orderInfo.totalAmount
                        )
                    )
                )
            )
        }
    }

    fun getAmountFormatted(amount: Int): String =
        amount.let {
            amountToCurrencyInteractor.execute(
                params = FormatCurrencyParams(
                    requireNotNull(
                        MonetaryAmount.create(
                            currency = basicOrderInfoInteractor(None).currencyCode,
                            value = amount
                        )
                    )
                )
            )
        }

    fun exchangePaymentMethodToken(
        paymentMethod: PrimerVaultedPaymentMethod,
        additionalData: PrimerVaultedPaymentMethodAdditionalData?
    ) = viewModelScope.launch {
        additionalData?.let {
            vaultManager.startPaymentFlow(vaultedPaymentMethodId = paymentMethod.id, additionalData = additionalData)
                .onFailure { throwable ->
                    handleError(throwable = throwable)
                }
        } ?: run {
            vaultManager.startPaymentFlow(vaultedPaymentMethodId = paymentMethod.id).onFailure { throwable ->
                handleError(throwable = throwable)
            }
        }
    }

    fun deletePaymentMethodToken(
        paymentMethod: PrimerVaultedPaymentMethod
    ) = viewModelScope.launch {
        vaultManager.deleteVaultedPaymentMethod(vaultedPaymentMethodId = paymentMethod.id)
            .onSuccess {
                _vaultedPaymentMethods.postValue(
                    _vaultedPaymentMethods.value.orEmpty()
                        .filterNot { vaultedPaymentMethod ->
                            vaultedPaymentMethod.analyticsId == paymentMethod.analyticsId
                        }
                )
            }
            .onFailure { throwable ->
                handleError(throwable = throwable)
            }
    }

    private suspend fun handleError(throwable: Throwable) {
        checkoutErrorHandler.handle(error = errorMapperRegistry.getPrimerError(throwable), payment = null)
    }
}
