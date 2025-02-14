package io.primer.components.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrimerCheckoutViewModel(
    private val clientToken: String,
) : ViewModel() {

    private val _paymentMethods = MutableStateFlow<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>(emptyList())
    val paymentMethods = _paymentMethods.asStateFlow()

    init {
        @Suppress("TooGenericExceptionCaught")
        viewModelScope.launch {
            try {
                loadPaymentMethods()
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadPaymentMethods() = try {
        println("loadPaymentMethods with clientToken: $clientToken")
        val paymentMethods = listOf(
            PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType = "PAYPAL",
                paymentMethodName = "Paypal",
                supportedPrimerSessionIntents = emptyList(),
                paymentMethodManagerCategories = emptyList(),
            ),
            PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType = "KLARNA",
                paymentMethodName = "Klarna",
                supportedPrimerSessionIntents = emptyList(),
                paymentMethodManagerCategories = emptyList(),
            ),
            PrimerHeadlessUniversalCheckoutPaymentMethod(
                paymentMethodType = "CARD",
                paymentMethodName = "Card",
                supportedPrimerSessionIntents = emptyList(),
                paymentMethodManagerCategories = emptyList(),
            ),
        )
        _paymentMethods.value = paymentMethods
    } catch (e: Exception) {
        println(e.message)
    }
}
