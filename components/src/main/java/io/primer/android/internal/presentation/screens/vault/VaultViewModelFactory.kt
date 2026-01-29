package io.primer.android.internal.presentation.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class VaultViewModelFactory : ViewModelProvider.Factory, DISdkComponent {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            return VaultViewModel(
                fetchVaultedPaymentMethodsUseCase = resolve(),
                submitVaultedPaymentUseCase = resolve(),
                checkCvvRecaptureRequiredUseCase = resolve(),
                deleteVaultedPaymentMethodUseCase = resolve(),
                componentsEventsRepository = resolve(),
                logReporter = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
