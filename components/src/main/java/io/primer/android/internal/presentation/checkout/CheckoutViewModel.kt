package io.primer.android.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.components.analytics.di.ComponentsAnalyticsContainer
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.domain.None
import io.primer.android.internal.di.ComponentsContainer
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.scope.PrimerCheckoutScope
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CheckoutViewModel(
    private val availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase,
    private val checkoutNavigator: CheckoutNavigator,
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor,
    private val configurationInteractor: ConfigurationInteractor,
    componentsEventsRepository: ComponentsEventsRepository? = null,
) : ViewModel(), PrimerCheckoutScope {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

    private val eventsRepository: ComponentsEventsRepository by lazy {
        componentsEventsRepository ?: DISdkContext.container().resolve()
    }

    init {
        val realStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            configurationInteractor.invoke(ConfigurationParams(CachePolicy.CacheFirst))
                .mapCatching { availablePaymentMethodsUseCase() }
                .mapCatching { basicOrderInfoInteractor(None) }
                .onSuccess {
                    eventsRepository.send(EventType.SdkInitStart, realStartTime)
                    eventsRepository.send(EventType.SdkInitEnd)
                    _state.value = PrimerCheckoutScope.State.Ready(
                        totalAmount = it.totalAmount,
                        currencyCode = it.currencyCode,
                    )
                    checkoutNavigator.navigateToPaymentMethodsList()
                }
                .onFailure {
                    // TODO: Consider adding EventType.SdkInitFailed for tracking initialization failures
                    _state.value = PrimerCheckoutScope.State.Error(it)
                    checkoutNavigator.navigateToError(it.message ?: "Failed to load payment methods")
                }
        }
    }

    override fun onDismiss() {
        eventsRepository.send(EventType.PaymentFlowExited)
        _state.value = PrimerCheckoutScope.State.Dismissed
    }

    override fun onCleared() {
        super.onCleared()
        DISdkContext.componentsSdkContainer?.apply {
            unregisterContainer<ComponentsContainer>()
            unregisterContainer<ComponentsAnalyticsContainer>()
            clear()
        }
        DISdkContext.componentsSdkContainer = null
    }

    override suspend fun onOtherPaymentMethods() {
        checkoutNavigator.navigateToPaymentMethodsList()
    }

    override suspend fun onRetry() {
        eventsRepository.send(EventType.PaymentReattempted)
        checkoutNavigator.navigateBack()
    }
}
