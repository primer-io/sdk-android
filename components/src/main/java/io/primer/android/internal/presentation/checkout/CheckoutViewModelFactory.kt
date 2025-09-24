package io.primer.android.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class CheckoutViewModelFactory : ViewModelProvider.Factory, DISdkComponent {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(
                availablePaymentMethodsUseCase = resolve(),
                checkoutNavigator = resolve(),
                componentsEventsRepository = null,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
