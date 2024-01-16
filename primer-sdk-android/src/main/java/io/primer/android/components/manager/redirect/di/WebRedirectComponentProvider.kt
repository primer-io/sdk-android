package io.primer.android.components.manager.redirect.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.manager.redirect.component.WebRedirectComponent
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve

internal object WebRedirectComponentProvider : DISdkComponent {
    fun provideInstance(
        owner: ViewModelStoreOwner,
        paymentMethodType: String
    ) = ViewModelProvider(
        owner = owner,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T = WebRedirectComponent(
                paymentMethodType = paymentMethodType,
                webRedirectDelegate = resolve(),
                loggingDelegate = resolve()
            ) as T
        }
    )[WebRedirectComponent::class.java]
}
