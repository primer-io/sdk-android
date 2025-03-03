package io.primer.components.ui.checkout

import androidx.lifecycle.ViewModel
import io.primer.components.Primer
import io.primer.components.models.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class PrimerPaymentMethodViewModel : ViewModel(),
    Primer.Scope.PaymentMethod {

    private val _state = MutableStateFlow<PaymentMethod.State?>(null)
    override val state = _state.asStateFlow()

}
