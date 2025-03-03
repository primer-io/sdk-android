package io.primer.components.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.components.Primer
import io.primer.components.models.PaymentMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PrimerCheckoutViewModel : ViewModel(), Primer.Scope.Checkout {

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    override val paymentMethods = _paymentMethods.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    override val selectedPaymentMethod = _selectedPaymentMethod.asStateFlow()

    init {
        viewModelScope.launch {
            loadPaymentMethods()
        }
    }

    private suspend fun loadPaymentMethods() {
        delay(100)
        _paymentMethods.value = listOf(
            PaymentMethod(name = "Google Pay", type = PaymentMethod.Type.GOOGLE_PAY),
            PaymentMethod(name = "Klarna", type = PaymentMethod.Type.KLARNA),
            PaymentMethod(name = "Card", type = PaymentMethod.Type.CARD)
        )
    }

    override fun selectPaymentMethod(method: PaymentMethod?) {
        _selectedPaymentMethod.value = method
    }

    override fun pay() {

    }

    override fun cancel() {

    }
}
