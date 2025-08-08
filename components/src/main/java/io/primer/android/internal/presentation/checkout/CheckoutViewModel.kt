package io.primer.android.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CheckoutViewModel(
    private val availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase,
    private val checkoutNavigator: CheckoutNavigator,
) : ViewModel(), PrimerCheckoutScope {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            availablePaymentMethodsUseCase().fold(
                onSuccess = {
                    _state.value = PrimerCheckoutScope.State.Ready
                    checkoutNavigator.navigateToPaymentMethodsList()
                },
                onFailure = {
                    _state.value = PrimerCheckoutScope.State.Error(it)
                    checkoutNavigator.navigateToError(it.message ?: "Failed to load payment methods")
                }
            )
        }
    }

    override fun onDismiss() {
        _state.value = PrimerCheckoutScope.State.Dismissed
    }

    override suspend fun onOtherPaymentMethods() {
        checkoutNavigator.navigateToPaymentMethodsList()
    }

    override suspend fun onRetry() {
        checkoutNavigator.navigateBack()
    }
}
