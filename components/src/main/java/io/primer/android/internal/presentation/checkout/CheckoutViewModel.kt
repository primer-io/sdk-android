package io.primer.android.internal.presentation.checkout

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethodsController
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.analytics.data.repository.ComponentsLoggingRepository
import io.primer.android.components.analytics.di.ComponentsAnalyticsContainer
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.DismissalMechanism
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.errors.domain.models.PrimerUnknownError
import io.primer.android.internal.di.ComponentsContainer
import io.primer.android.internal.domain.Cleanable
import io.primer.android.internal.domain.error.PrimerErrorException
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.HeadlessCleanupUseCase
import io.primer.android.internal.domain.usecase.vault.CheckCvvRecaptureRequiredUseCase
import io.primer.android.internal.domain.usecase.vault.DeleteVaultedPaymentMethodUseCase
import io.primer.android.internal.domain.usecase.vault.FetchVaultedPaymentMethodsUseCase
import io.primer.android.internal.domain.usecase.vault.SubmitVaultedPaymentUseCase
import io.primer.android.internal.navigation.CheckoutNavigator
import io.primer.android.internal.navigation.CountryNavigator
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.core.domain.FormatAmountToCurrencyInteractor
import io.primer.cardShared.CardNumberFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Internal ViewModel implementing [PrimerCheckoutController].
 *
 * Uses existing SDK models directly:
 * - [PrimerComposablePaymentMethod] for payment methods
 * - [PrimerVaultedPaymentMethod] for vaulted payment methods
 *
 * Navigation and result delivery are delegated to [CheckoutNavigator].
 */
@Suppress("LongParameterList", "TooManyFunctions")
internal class CheckoutViewModel(
    internal val settings: PrimerSettings,
    private val configurationInteractor: ConfigurationInteractor,
    private val configurationRepository: ConfigurationRepository,
    private val availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase,
    private val cleanupUseCase: HeadlessCleanupUseCase,
    private val fetchVaultedPaymentMethodsUseCaseProvider: () -> FetchVaultedPaymentMethodsUseCase,
    private val checkCvvRecaptureRequiredUseCaseProvider: () -> CheckCvvRecaptureRequiredUseCase,
    private val submitVaultedPaymentUseCaseProvider: () -> SubmitVaultedPaymentUseCase,
    private val deleteVaultedPaymentMethodUseCaseProvider: () -> DeleteVaultedPaymentMethodUseCase,
    private val formatAmountInteractor: FormatAmountToCurrencyInteractor,
    private val componentsEventsRepository: ComponentsEventsRepository?,
    private val logReporter: LogReporter,
    internal val navigator: CheckoutNavigator,
    internal val countryNavigator: CountryNavigator,
) : ViewModel(), PrimerCheckoutController, PrimerPaymentMethodsController {

    private val loggingRepository: ComponentsLoggingRepository? by lazy {
        DISdkContext.componentsSdkContainer?.resolve<ComponentsLoggingRepository>()
    }

    @Immutable
    data class CvvState(
        val value: String = "",
        val isValid: Boolean = false,
    )

    private val _paymentMethods = MutableStateFlow<List<PrimerComposablePaymentMethod>>(emptyList())
    override val paymentMethods: StateFlow<List<PrimerComposablePaymentMethod>> = _paymentMethods.asStateFlow()

    private val _vaultedPaymentMethods = MutableStateFlow<List<PrimerVaultedPaymentMethod>>(emptyList())
    val vaultedPaymentMethods: StateFlow<List<PrimerVaultedPaymentMethod>> =
        _vaultedPaymentMethods.asStateFlow()

    // Combined checkout state - created internally, shared with navigator
    private val _state = MutableStateFlow<PrimerCheckoutState>(PrimerCheckoutState.Loading)
    override val state: StateFlow<PrimerCheckoutState> = _state.asStateFlow()

    init {
        initialize()
        observeNavigationEvents()
    }

    /**
     * Observes navigation events and updates state accordingly.
     * This keeps state management centralized in the ViewModel.
     */
    private fun observeNavigationEvents() {
        viewModelScope.launch {
            navigator.navigationEvents.collect { event ->
                when (event) {
                    is CheckoutNavigator.NavigationEvent.NavigateToSuccess -> {
                        _state.value = PrimerCheckoutState.Success(event.checkoutData)
                    }
                    is CheckoutNavigator.NavigationEvent.NavigateToError -> {
                        _state.value = PrimerCheckoutState.Failure(event.error)
                    }
                    is CheckoutNavigator.NavigationEvent.Dismiss -> {
                        _state.value = PrimerCheckoutState.Cancelled
                    }
                    else -> {
                        // Other navigation events don't affect checkout state
                    }
                }
            }
        }
    }

    // Initialization event - emitted when checkout is ready
    sealed interface InitEvent {
        data object Ready : InitEvent
        data object Error : InitEvent
    }

    private val _initEvent = Channel<InitEvent>(Channel.BUFFERED)
    val initEvent = _initEvent.receiveAsFlow()

    // CVV state
    private val _cvvState = MutableStateFlow(CvvState())
    val cvvState: StateFlow<CvvState> = _cvvState.asStateFlow()

    // Currently selected vaulted method for CVV recapture
    private val _selectedVaultedMethodForCvv = MutableStateFlow<PrimerVaultedPaymentMethod?>(null)
    val selectedVaultedMethodForCvv: StateFlow<PrimerVaultedPaymentMethod?> = _selectedVaultedMethodForCvv.asStateFlow()

    // Vault payment loading state
    private val _isVaultPaymentLoading = MutableStateFlow(false)
    val isVaultPaymentLoading: StateFlow<Boolean> = _isVaultPaymentLoading.asStateFlow()

    internal val isSwipeEnabled = settings.uiOptions.dismissalMechanism.contains(DismissalMechanism.GESTURES)

    // Track the currently active payment flow for cleanup
    private var activeCleanable: Cleanable? = null

    /**
     * Register a cleanable as the active payment flow.
     * Called when a payment flow starts. Only one flow can be active at a time.
     */
    internal fun setActiveFlow(cleanable: Cleanable) {
        activeCleanable = cleanable
    }

    override fun refresh() {
        _state.value = PrimerCheckoutState.Loading
        viewModelScope.launch {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceNetwork))
                .mapCatching {
                    // Refresh available payment methods
                    availablePaymentMethodsUseCase().getOrThrow()
                    _paymentMethods.value = availablePaymentMethodsUseCase.cache

                    // Refresh vaulted payment methods
                    fetchVaultedPaymentMethodsUseCaseProvider()()
                        .onSuccess { vaulted ->
                            _vaultedPaymentMethods.value = vaulted
                        }

                    // Refresh client session data
                    val clientSessionData = configurationRepository.getConfiguration()
                        .clientSession.clientSessionDataResponse.toClientSessionData()
                    clientSessionData.clientSession
                }
                .onSuccess { clientSession ->
                    _state.value = PrimerCheckoutState.Ready(clientSession)
                    _initEvent.trySend(InitEvent.Ready)
                }
                .onFailure { throwable ->
                    val primerError = (throwable as? PrimerErrorException)?.primerError
                        ?: PrimerUnknownError(throwable.message ?: "Failed to refresh client session")
                    _state.value = PrimerCheckoutState.Failure(primerError)
                    _initEvent.trySend(InitEvent.Error)
                }
        }
    }

    override fun select(method: PrimerComposablePaymentMethod) {
        val type = PaymentMethodType.safeValueOf(method.paymentMethodType)
        if (type == PaymentMethodType.PAYMENT_CARD) {
            navigator.navigateToCardForm()
        } else {
            navigator.startPaymentFlow(method.paymentMethodType)
        }
    }

    internal fun selectVaultedPaymentMethod(method: PrimerVaultedPaymentMethod) {
        navigator.startVaultedPaymentFlow(method)
    }

    internal fun deleteVaultedPaymentMethod(method: PrimerVaultedPaymentMethod) {
        viewModelScope.launch {
            deleteVaultedPaymentMethodUseCaseProvider()(method.id).fold(
                onSuccess = {
                    _vaultedPaymentMethods.update { methods -> methods - method }
                },
                onFailure = { /* Deletion failed - could expose error via state if needed */ },
            )
        }
    }

    // CVV recapture methods
    internal fun checkCvvAndStartVaultedPayment(method: PrimerVaultedPaymentMethod) {
        viewModelScope.launch {
            val needsCvv = checkCvvRecaptureRequiredUseCaseProvider()(method)
            if (needsCvv) {
                _selectedVaultedMethodForCvv.value = method
                _cvvState.value = CvvState()
                navigator.showCvvRecapture(method)
            } else {
                // No CVV needed, proceed with payment directly
                submitVaultedPaymentInternal(method, null)
            }
        }
    }

    internal fun updateCvv(cvv: String) {
        val method = _selectedVaultedMethodForCvv.value ?: return
        val first6 = method.paymentInstrumentData.first6Digits?.toString().orEmpty()
        val expectedLength = CardNumberFormatter.fromString(first6).getCvvLength()
        val filteredCvv = cvv.filter { it.isDigit() }.take(expectedLength)
        val isValid = filteredCvv.length == expectedLength
        _cvvState.value = CvvState(filteredCvv, isValid)
    }

    /**
     * Submit the vaulted payment with the entered CVV.
     * Called from the CVV recapture screen.
     */
    internal fun submitVaultedPaymentWithCvv() {
        val method = _selectedVaultedMethodForCvv.value ?: return
        val cvv = _cvvState.value.value.takeIf { _cvvState.value.isValid }
        submitVaultedPaymentInternal(method, cvv)
    }

    /**
     * Format amount in minor units to a currency string.
     * Uses client session currency for formatting.
     */
    internal fun formatAmount(amountInCents: Int): String {
        val currency = when (val currentState = _state.value) {
            is PrimerCheckoutState.Ready -> currentState.clientSession.currencyCode
            else -> null
        }
        return formatAmountInteractor(
            FormatCurrencyParams(requireNotNull(MonetaryAmount.create(requireNotNull(currency), amountInCents))),
        )
    }

    /**
     * Cleans up any active payment flow.
     * Called when checkout sheet/host leaves composition (modal dismissed, user navigates away).
     * This cancels in-flight requests but keeps the SDK ready for another attempt.
     */
    internal fun cleanupActiveFlow() {
        activeCleanable?.let { cleanable ->
            logReporter.debug("Cleaning up active payment flow", component = TAG)
            cleanable.cleanup()
            activeCleanable = null
        }
    }

    override fun onCleared() {
        DISdkContext.componentsSdkContainer?.apply {
            unregisterContainer<ComponentsContainer>()
            unregisterContainer<ComponentsAnalyticsContainer>()
            clear()
        }
        DISdkContext.componentsSdkContainer = null
        cleanupUseCase()
    }

    private fun initialize() {
        val realStartTime = System.currentTimeMillis()
        logReporter.info("Initializing checkout", component = TAG)

        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.CacheFirst))
                .mapCatching {
                    logReporter.debug("Configuration loaded successfully", component = TAG)
                    observeCrashes()

                    availablePaymentMethodsUseCase().getOrThrow()
                    _paymentMethods.value = availablePaymentMethodsUseCase.cache

                    logReporter.debug(
                        "Available payment methods: ${availablePaymentMethodsUseCase.cache.size}",
                        component = TAG,
                    )
                    fetchVaultedPaymentMethodsUseCaseProvider()()
                        .onSuccess { vaulted ->
                            _vaultedPaymentMethods.value = vaulted
                        }

                    // Get client session data
                    val clientSessionData = configurationRepository.getConfiguration()
                        .clientSession.clientSessionDataResponse.toClientSessionData()
                    clientSessionData.clientSession
                }
                .onSuccess { clientSession ->
                    componentsEventsRepository?.send(EventType.SdkInitStart, startTime)
                    componentsEventsRepository?.send(EventType.SdkInitEnd)
                    loggingRepository?.sendInfoLog(
                        message = "Checkout components initialized",
                        event = "checkout_components_initialized",
                        initDurationMs = System.currentTimeMillis() - realStartTime,
                    )
                    _state.value = PrimerCheckoutState.Ready(clientSession)
                    _initEvent.trySend(InitEvent.Ready)
                }
                .onFailure { throwable ->
                    val primerError = (throwable as? PrimerErrorException)?.primerError
                        ?: PrimerUnknownError(throwable.message ?: "Failed to initialize checkout")
                    _state.value = PrimerCheckoutState.Failure(primerError)
                    _initEvent.trySend(InitEvent.Error)
                }
        }
    }

    private fun submitVaultedPaymentInternal(method: PrimerVaultedPaymentMethod, cvv: String?) {
        viewModelScope.launch {
            _isVaultPaymentLoading.value = true
            navigator.navigateToLoading()
            componentsEventsRepository?.send(EventType.PaymentSubmitted(method.paymentMethodType))

            submitVaultedPaymentUseCaseProvider()(method.id, cvv).fold(
                onSuccess = { checkoutData ->
                    componentsEventsRepository?.send(
                        EventType.PaymentSuccess(method.paymentMethodType, checkoutData.payment.id),
                    )
                    _isVaultPaymentLoading.value = false
                    _cvvState.value = CvvState()
                    _selectedVaultedMethodForCvv.value = null
                    navigator.onSuccess(checkoutData)
                },
                onFailure = { error ->
                    componentsEventsRepository?.send(EventType.PaymentFailure(method.paymentMethodType))
                    _isVaultPaymentLoading.value = false
                    val primerError = (error as? PrimerErrorException)?.primerError
                        ?: PrimerUnknownError(error.message ?: "Payment failed")
                    navigator.onError(primerError)
                },
            )
        }
    }

    private fun observeCrashes() {
        DISdkContext.componentsSdkContainer
            ?.resolve<ComponentsLoggingRepository>()
            ?.observeCrashes()
        logReporter.debug("Crash logging observation started", component = TAG)
    }

    companion object Companion {
        private const val TAG = "CheckoutViewModel"
    }
}
