package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.domain.None
import io.primer.android.internal.domain.usecase.AvailablePaymentMethodsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PaymentMethodSelectionViewModel(
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor,
    private val checkoutNavigator: CheckoutNavigator,
    private val availablePaymentMethodsUseCase: AvailablePaymentMethodsUseCase,
) : ViewModel(), PrimerPaymentMethodSelectionScope {

    private val _uiState = MutableStateFlow(PrimerPaymentMethodSelectionScope.State())
    override val state: StateFlow<PrimerPaymentMethodSelectionScope.State> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        val orderInfo = basicOrderInfoInteractor(None)
        _uiState.value = PrimerPaymentMethodSelectionScope.State(
            availablePaymentMethodsUseCase.cache,
            orderInfo,
        )
    }

    override fun onPaymentMethodSelected(paymentMethod: String) {
        viewModelScope.launch {
            when (paymentMethod) {
                // TODO add rest of payment methods
                PaymentMethodType.PAYMENT_CARD.name -> {
                    checkoutNavigator.navigateTo(Screen.CardForm)
                }
            }
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
