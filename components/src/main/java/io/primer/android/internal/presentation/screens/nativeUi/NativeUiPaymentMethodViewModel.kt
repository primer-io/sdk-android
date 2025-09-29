package io.primer.android.internal.presentation.screens.nativeUi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.Screen
import io.primer.android.scope.PrimerNativeUiPaymentMethodScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class NativeUiPaymentMethodViewModel(
    private val paymentMethodType: String,
    private val checkoutNavigator: CheckoutNavigator,
    private val startNativeUiPaymentUseCase: StartNativeUiPaymentUseCase,
) : ViewModel(), PrimerNativeUiPaymentMethodScope {

    private val _state = MutableStateFlow(PrimerNativeUiPaymentMethodScope.State())
    override val state: StateFlow<PrimerNativeUiPaymentMethodScope.State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true)

            startNativeUiPaymentUseCase(paymentMethodType).fold(
                onSuccess = {
                    _state.value = _state.value.copy(isProcessing = false)
                    checkoutNavigator.navigateTo(Screen.Success)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isProcessing = false,
                        error = error.message,
                    )
                    checkoutNavigator.navigateTo(Screen.Error)
                },
            )
        }
    }

    override fun onCancel() {
        viewModelScope.launch {
            checkoutNavigator.dismiss()
        }
    }

    override fun onCleared() {
        super.onCleared()
        startNativeUiPaymentUseCase.cleanup()
    }
}
