package io.primer.android.internal.presentation.screens.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class SelectCountryViewModelFactory : ViewModelProvider.Factory, DISdkComponent {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectCountryViewModel::class.java)) {
            return SelectCountryViewModel(
                countriesRepository = resolve(),
                checkoutNavigator = resolve(),
                logReporter = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
