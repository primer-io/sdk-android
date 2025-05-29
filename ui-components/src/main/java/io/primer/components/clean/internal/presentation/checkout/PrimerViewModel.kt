package io.primer.components.clean.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.components.Primer
import io.primer.components.clean.internal.domain.usecases.GetAvailablePaymentMethodsUseCase
import io.primer.components.clean.model.PrimerPaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PrimerViewModel : ViewModel(), Primer, DISdkComponent {

    private val getAvailablePaymentMethodsUseCase : GetAvailablePaymentMethodsUseCase by lazy {
        resolve()
    }

    private val _uiState = MutableStateFlow<Primer.State>(Primer.State.Loading)
    override val state: StateFlow<Primer.State> = _uiState.asStateFlow()

    init { loadPaymentMethods() }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            getAvailablePaymentMethodsUseCase().fold(
                onSuccess = { methods ->
                    _uiState.value = Primer.State.Ready(methods)
                },
                onFailure = { error ->
                    _uiState.value = Primer.State.Error(error.message.orEmpty())
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
}
