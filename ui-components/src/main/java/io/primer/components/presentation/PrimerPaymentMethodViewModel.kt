package io.primer.components.presentation

import androidx.lifecycle.ViewModel
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrimerPaymentMethodViewModel : ViewModel() {

    private val _selectedMethod = MutableStateFlow<PrimerHeadlessUniversalCheckoutPaymentMethod?>(null)
    val selectedMethod = _selectedMethod.asStateFlow()

    fun selectMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?) {
        _selectedMethod.value = method
    }
}
