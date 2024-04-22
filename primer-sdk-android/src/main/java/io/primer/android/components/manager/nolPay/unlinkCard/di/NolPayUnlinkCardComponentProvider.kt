package io.primer.android.components.manager.nolPay.unlinkCard.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.manager.nolPay.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DISdkComponent
import io.primer.android.di.NolPayContainer.Companion.NOL_PAY_ERROR_RESOLVER_NAME
import io.primer.android.di.extension.resolve

internal class NolPayUnlinkCardComponentProvider : DISdkComponent {

    fun provideInstance(owner: ViewModelStoreOwner): NolPayUnlinkCardComponent {
        return ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return NolPayUnlinkCardComponent(
                        resolve(),
                        resolve(PrimerPaymentMethodManagerCategory.NOL_PAY.name),
                        resolve(),
                        resolve(),
                        resolve(NOL_PAY_ERROR_RESOLVER_NAME),
                        extras.createSavedStateHandle()
                    ) as T
                }
            }
        ).get(
            key = runCatching {
                resolve<PrimerConfig>().clientTokenBase64.orEmpty()
            }.getOrNull() ?: NolPayUnlinkCardComponent::class.java.canonicalName,
            modelClass = NolPayUnlinkCardComponent::class.java
        )
    }
}
