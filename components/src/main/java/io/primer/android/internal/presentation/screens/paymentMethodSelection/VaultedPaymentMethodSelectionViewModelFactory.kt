package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

/**
 * Factory for creating VaultedPaymentMethodSelectionViewModel instances with proper dependency injection.
 */
internal class VaultedPaymentMethodSelectionViewModelFactory : ViewModelProvider.Factory, DISdkComponent {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultedPaymentMethodSelectionViewModel::class.java)) {
            return VaultedPaymentMethodSelectionViewModel(
                fetchVaultedPaymentMethodsUseCase = resolve(),
                submitVaultedPaymentUseCase = resolve(),
                validateVaultedCVVUseCase = resolve(),
                shouldCaptureVaultedCvvUseCase = resolve(),
                cvvFieldsUseCase = resolve(),
                componentsEventsRepository = resolve(),
                checkoutNavigator = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
