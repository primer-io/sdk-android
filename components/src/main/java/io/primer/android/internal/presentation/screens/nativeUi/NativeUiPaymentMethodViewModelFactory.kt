package io.primer.android.internal.presentation.screens.nativeUi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class NativeUiPaymentMethodViewModelFactory(
    private val paymentMethodType: String,
) : ViewModelProvider.Factory, DISdkComponent {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NativeUiPaymentMethodViewModel::class.java)) {
            return NativeUiPaymentMethodViewModel(
                paymentMethodType = paymentMethodType,
                startNativeUiPaymentUseCase = resolve(),
                logReporter = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
