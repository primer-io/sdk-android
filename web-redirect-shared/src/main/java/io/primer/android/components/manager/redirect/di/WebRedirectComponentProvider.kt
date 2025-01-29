package io.primer.android.components.manager.redirect.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.manager.formWithRedirect.component.PrimerHeadlessRedirectComponent
import io.primer.android.components.manager.formWithRedirect.composable.RedirectCollectableData
import io.primer.android.components.manager.redirect.component.WebRedirectComponent
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve

object WebRedirectComponentProvider : DISdkComponent {
    fun provideInstance(
        owner: ViewModelStoreOwner,
        paymentMethodType: String,
    ): PrimerHeadlessRedirectComponent<RedirectCollectableData, WebRedirectStep> {
        return ViewModelProvider(
            owner = owner,
            factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras,
                ): T =
                    WebRedirectComponent(
                        paymentMethodType = paymentMethodType,
                        webRedirectDelegate = resolve(),
                        loggingDelegate = resolve(),
                    ) as T
            },
        )[WebRedirectComponent::class.java]
    }
}
