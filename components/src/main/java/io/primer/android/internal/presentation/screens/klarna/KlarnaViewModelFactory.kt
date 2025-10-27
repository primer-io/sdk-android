package io.primer.android.internal.presentation.screens.klarna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

internal class KlarnaViewModelFactory(
    private val viewModelStoreOwner: ViewModelStoreOwner,
) : ViewModelProvider.Factory, DISdkComponent {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KlarnaViewModel::class.java)) {
            return KlarnaViewModel(
                viewModelStoreOwner = viewModelStoreOwner,
                klarnaRepository = resolve(),
                checkoutNavigator = resolve(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
