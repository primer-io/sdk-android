package io.primer.components.implementation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.components.PrimerCheckoutScope
import io.primer.components.models.CardPaymentMethod
import io.primer.components.models.KlarnaPaymentMethod
import io.primer.components.models.PaymentMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PrimerCheckoutViewModel : ViewModel(), PrimerCheckoutScope {

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod<*>>>(emptyList())
    override val paymentMethods = _paymentMethods.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod<*>?>(null)
    override val selectedPaymentMethod = _selectedPaymentMethod.asStateFlow()

    init {
        viewModelScope.launch {
            loadPaymentMethods()
        }
    }

    private suspend fun loadPaymentMethods() {
        delay(100)
        _paymentMethods.value = listOf(
            KlarnaPaymentMethod(),
            CardPaymentMethod(),
        )
    }

    override fun selectPaymentMethod(method: PaymentMethod<*>?) {
        _selectedPaymentMethod.value = method
    }
}
