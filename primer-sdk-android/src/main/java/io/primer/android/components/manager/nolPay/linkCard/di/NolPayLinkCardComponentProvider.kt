package io.primer.android.components.manager.nolPay.linkCard.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.nolPay.linkCard.component.NolPayLinkCardComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DISdkComponent
import io.primer.android.di.NolPayContainer.Companion.NOL_PAY_ERROR_RESOLVER_NAME
import io.primer.android.di.extension.resolve

internal class NolPayLinkCardComponentProvider : DISdkComponent {

    fun provideInstance(owner: ViewModelStoreOwner): NolPayLinkCardComponent {
        return ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return NolPayLinkCardComponent(
                        linkPaymentCardDelegate = resolve(),
                        validatorRegistry = resolve(),
                        eventLoggingDelegate = resolve(PrimerPaymentMethodManagerCategory.NOL_PAY.name),
                        errorLoggingDelegate = resolve(),
                        validationErrorLoggingDelegate = resolve(),
                        errorMapper = resolve(NOL_PAY_ERROR_RESOLVER_NAME),
                        savedStateHandle = extras.createSavedStateHandle()
                    ) as T
                }
            }
        ).get(
            key = runCatching {
                resolve<PrimerConfig>().clientTokenBase64.orEmpty()
            }.getOrNull() ?: NolPayLinkCardComponent::class.java.canonicalName,
            modelClass = NolPayLinkCardComponent::class.java
        )
    }
}
