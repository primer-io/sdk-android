package io.primer.android.internal.presentation.checkout

import io.primer.android.core.di.DISdkComponent
import io.primer.android.internal.presentation.scope.DefaultCheckoutScope
import io.primer.android.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class CheckoutViewModel : DefaultCheckoutScope(), DISdkComponent {

    private val _state =
        MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Initializing)
    override val state: StateFlow<PrimerCheckoutScope.State> = _state.asStateFlow()

    override fun onDismiss() {
        _state.value = PrimerCheckoutScope.State.Dismissed
    }
}
