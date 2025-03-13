package io.primer.components.implementation.base

import androidx.lifecycle.ViewModel
import io.primer.android.core.di.DISdkComponent
import io.primer.components.PrimerPaymentMethodScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal abstract class PaymentMethodViewModel<T : PrimerPaymentMethodScope.PrimerPaymentMethodUiState> :
    ViewModel(),
    PrimerPaymentMethodScope<T>,
    DISdkComponent {
    @Suppress("VariableNaming")
    protected val _state = MutableStateFlow<T?>(null)
    override val state = _state.asStateFlow()
}
