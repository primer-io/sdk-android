package io.primer.android.internal.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.configuration.di.ConfigurationCoreContainer
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class CheckoutViewModelFactory : ViewModelProvider.Factory, DISdkComponent {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(
                availablePaymentMethodsUseCase = resolve(),
                checkoutNavigator = resolve(),
                basicOrderInfoInteractor = resolve(),
                configurationInteractor = resolve(ConfigurationCoreContainer.CONFIGURATION_INTERACTOR_DI_KEY),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
