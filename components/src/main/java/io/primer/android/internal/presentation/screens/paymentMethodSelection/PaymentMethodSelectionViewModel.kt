package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.core.domain.None
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfoInteractor
import io.primer.android.internal.domain.usecase.GetAvailablePaymentMethodsUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.internal.presentation.scope.DefaultPaymentMethodSelectionScope
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PaymentMethodSelectionViewModel : DefaultPaymentMethodSelectionScope(), DISdkComponent {

    private val getAvailablePaymentMethodsUseCase: GetAvailablePaymentMethodsUseCase by lazy { resolve() }
    private val basicOrderInfoInteractor: BasicOrderInfoInteractor by lazy { resolve() }
    private val checkoutNavigator: CheckoutNavigator by lazy { resolve() }

    private val _uiState =
        MutableStateFlow<PrimerPaymentMethodSelectionScope.State>(PrimerPaymentMethodSelectionScope.State.Loading)
    override val state: StateFlow<PrimerPaymentMethodSelectionScope.State> = _uiState.asStateFlow()

    init { loadPaymentMethods() }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            getAvailablePaymentMethodsUseCase().fold(
                onSuccess = { methods ->
                    val orderInfo = basicOrderInfoInteractor(None)
                    _uiState.value = PrimerPaymentMethodSelectionScope.State.Ready(methods, orderInfo)
                },
                onFailure = { error ->
                    _uiState.value = PrimerPaymentMethodSelectionScope.State.Error(error)
                },
            )
        }
    }

    override fun onPaymentMethodSelected(paymentMethod: String) {
        viewModelScope.launch {
            when (paymentMethod) {
                PaymentMethodType.PAYMENT_CARD.name -> checkoutNavigator.navigateTo(Screen.CardForm)
                // TODO add rest of screens
            }
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }
}
