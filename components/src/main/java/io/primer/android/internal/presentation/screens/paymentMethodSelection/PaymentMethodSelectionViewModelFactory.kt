package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class PaymentMethodSelectionViewModelFactory : ViewModelProvider.Factory, DISdkComponent {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentMethodSelectionViewModel::class.java)) {
            return PaymentMethodSelectionViewModel(
                basicOrderInfoInteractor = resolve(),
                checkoutNavigator = resolve(),
                availablePaymentMethodsUseCase = resolve(),
                formatAmountToCurrencyInteractor = resolve(),
                componentsEventsRepository = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
