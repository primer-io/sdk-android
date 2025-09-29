package io.primer.android.internal.presentation.screens.nativeUi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.internal.domain.usecase.StartNativeUiPaymentUseCase
import io.primer.android.internal.presentation.checkout.CheckoutNavigator

internal class NativeUiPaymentMethodViewModelFactory(
    private val paymentMethodType: String,
    private val checkoutNavigator: CheckoutNavigator,
    private val startNativeUiPaymentUseCase: StartNativeUiPaymentUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NativeUiPaymentMethodViewModel::class.java)) {
            return NativeUiPaymentMethodViewModel(
                paymentMethodType = paymentMethodType,
                checkoutNavigator = checkoutNavigator,
                startNativeUiPaymentUseCase = startNativeUiPaymentUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
