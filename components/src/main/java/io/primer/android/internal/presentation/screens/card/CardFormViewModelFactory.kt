package io.primer.android.internal.presentation.screens.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class CardFormViewModelFactory : ViewModelProvider.Factory, DISdkComponent {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardFormViewModel::class.java)) {
            return CardFormViewModel(
                cardFieldsUseCase = resolve(),
                cardNetworkUseCase = resolve(),
                submitCardPaymentUseCase = resolve(),
                checkoutNavigator = resolve(),
                logReporter = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
