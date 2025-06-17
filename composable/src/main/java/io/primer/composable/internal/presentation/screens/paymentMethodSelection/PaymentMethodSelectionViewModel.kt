package io.primer.composable.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.domain.None
import io.primer.composable.internal.domain.interactor.GetAvailablePaymentMethodsInteractor
import io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.composable.internal.presentation.checkout.CheckoutNavigator
import io.primer.composable.internal.presentation.checkout.Screen
import io.primer.composable.scope.PaymentMethodSelectionScope
import io.primer.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentMethodSelectionViewModel : ViewModel(), PaymentMethodSelectionScope, DISdkComponent {

    private val getAvailablePaymentMethodsInteractor: GetAvailablePaymentMethodsInteractor by lazy { resolve() }
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }

    private val _uiState =
        MutableStateFlow<PaymentMethodSelectionScope.State>(PaymentMethodSelectionScope.State.Loading)
    override val state: StateFlow<PaymentMethodSelectionScope.State> = _uiState.asStateFlow()

    init { loadPaymentMethods() }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            getAvailablePaymentMethodsInteractor().fold(
                onSuccess = { methods ->
                    val orderInfo = basicOrderInfoInteractor(None)
                    val currency = orderInfo.currencyCode.let { currencyCode ->
                        try {
                            java.util.Currency.getInstance(currencyCode)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _uiState.value = PaymentMethodSelectionScope.State.Ready(methods, currency)
                },
                onFailure = { error ->
                    _uiState.value = PaymentMethodSelectionScope.State.Error(error)
                },
            )
        }
    }

    override fun onPaymentMethodSelected(paymentMethod: PrimerComposablePaymentMethod) {
        viewModelScope.launch {
            checkoutNavigator.navigateTo(Screen.CardForm)
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
