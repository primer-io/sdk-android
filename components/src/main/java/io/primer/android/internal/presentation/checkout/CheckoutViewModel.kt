package io.primer.android.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.analytics.data.model.EventType
import io.primer.android.components.analytics.data.repository.ComponentsEventsRepository
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.core.domain.None
import io.primer.android.core.extensions.flatMap
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
    private val componentsEventsRepository: ComponentsEventsRepository?, // TODO Darius fix the DI to send events
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor,
    private val configurationRepository: ConfigurationRepository,
) : ViewModel(), PrimerCheckoutScope {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            configurationRepository.fetchConfiguration(CachePolicy.CacheFirst)
                .flatMap { availablePaymentMethodsUseCase() }
                .onSuccess {
                    val orderInfo = basicOrderInfoInteractor(None)
                    _state.value = PrimerCheckoutScope.State.Ready(
                        totalAmount = orderInfo.totalAmount,
                        currencyCode = orderInfo.currencyCode,
                    )
                    componentsEventsRepository?.send(EventType.CHECKOUT_FLOW_STARTED)
                    checkoutNavigator.navigateToPaymentMethodsList()
                }
                .onFailure {
                    _state.value = PrimerCheckoutScope.State.Error(it)
                    checkoutNavigator.navigateToError(it.message ?: "Failed to load payment methods")
                }
        }
    }

    override fun onDismiss() {
        componentsEventsRepository?.send(EventType.PAYMENT_FLOW_EXITED)
        _state.value = PrimerCheckoutScope.State.Dismissed
    }

    override suspend fun onOtherPaymentMethods() {
        checkoutNavigator.navigateToPaymentMethodsList()
    }

    override suspend fun onRetry() {
        componentsEventsRepository?.send(EventType.PAYMENT_REATTEMPTED)
        checkoutNavigator.navigateBack()
    }
}
