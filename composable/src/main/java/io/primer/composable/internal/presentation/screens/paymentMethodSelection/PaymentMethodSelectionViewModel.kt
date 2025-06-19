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
import io.primer.composable.internal.presentation.utils.CurrencyFormatter
import io.primer.composable.internal.scope.PaymentMethodSelectionScopeDefaults
import io.primer.composable.scope.PrimerPaymentMethodSelectionScope
import io.primer.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentMethodSelectionViewModel : ViewModel(), PaymentMethodSelectionScopeDefaults(), DISdkComponent {

    private val getAvailablePaymentMethodsInteractor: GetAvailablePaymentMethodsInteractor by lazy { resolve() }
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }

    private val _uiState =
        MutableStateFlow<PrimerPaymentMethodSelectionScope.State>(PrimerPaymentMethodSelectionScope.State.Loading)
    override val state: StateFlow<PrimerPaymentMethodSelectionScope.State> = _uiState.asStateFlow()

    // TODO COMPOSABLE move this to a separate function in scope
    init { loadPaymentMethods() }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            getAvailablePaymentMethodsInteractor().fold(
                onSuccess = { methods ->
                    val orderInfo = basicOrderInfoInteractor(None)
                    val title = CurrencyFormatter.formatTitle(orderInfo.totalAmount, orderInfo.currencyCode)
                    _uiState.value = PrimerPaymentMethodSelectionScope.State.Ready(methods, title)
                },
                onFailure = { error ->
                    _uiState.value = PrimerPaymentMethodSelectionScope.State.Error(error)
                },
            )
        }
    }

    override fun onPaymentMethodSelected(paymentMethod: PrimerComposablePaymentMethod) {
        viewModelScope.launch {
            // TODO COMPOSABLE make it dynamic
            checkoutNavigator.navigateTo(Screen.CardForm)
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
