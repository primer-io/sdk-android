package io.primer.android.components.manager.nolPay.payment.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.manager.nolPay.payment.component.NolPayPaymentComponent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.di.DISdkComponent
import io.primer.android.di.NolPayContainer.Companion.NOL_PAY_ERROR_RESOLVER_NAME
import io.primer.android.di.extension.resolve

internal class NolPayStartPaymentComponentProvider : DISdkComponent {

    fun provideInstance(owner: ViewModelStoreOwner): NolPayPaymentComponent {
        return ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return NolPayPaymentComponent(
                        resolve(),
                        resolve(),
                        resolve(),
                        resolve(NOL_PAY_ERROR_RESOLVER_NAME)
                    ) as T
                }
            }
        ).get(
            key = runCatching {
                resolve<PrimerConfig>().clientTokenBase64.orEmpty()
            }.getOrNull() ?: NolPayPaymentComponent::class.java.canonicalName,
            modelClass = NolPayPaymentComponent::class.java
        )
    }
}
