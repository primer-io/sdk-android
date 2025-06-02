package io.primer.composable.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.di.ComposableSdk
import io.primer.composable.internal.domain.usecases.GetAvailablePaymentMethodsUseCase
import io.primer.composable.model.PrimerPaymentMethod
import io.primer.composable.scope.PrimerCheckoutScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class CheckoutViewModel : ViewModel(), PrimerCheckoutScope, DISdkComponent {

    private val getAvailablePaymentMethodsUseCase : GetAvailablePaymentMethodsUseCase by lazy {
        resolve()
    }

    private val _uiState = MutableStateFlow<PrimerCheckoutScope.State>(PrimerCheckoutScope.State.Loading)
    override val state: StateFlow<PrimerCheckoutScope.State> = _uiState.asStateFlow()

    init { loadPaymentMethods() }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            getAvailablePaymentMethodsUseCase().fold(
                onSuccess = { methods ->
                    _uiState.value = PrimerCheckoutScope.State.Ready(methods)
                },
                onFailure = { error ->
//                    _uiState.value = PrimerCheckoutScope.State.Error(PrimerError())
                }
            )
        }
    }

    override fun selectPaymentMethod(method: PrimerPaymentMethod) {
//        _uiState.value = _uiState.value.copy(
//            selectedPaymentMethod = method
//        )
    }

    override fun clearSelectedPaymentMethod() {
//        _uiState.value = _uiState.value.copy(
//            selectedPaymentMethod = null
//        )
    }

    override fun cleanup() {
        ComposableSdk.cleanup()
    }
}
