package io.primer.android.nolpay.api.manager.unlinkCard.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.nolpay.api.manager.unlinkCard.component.NolPayUnlinkCardComponent
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

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
                        unlinkPaymentCardDelegate = resolve(),
                        eventLoggingDelegate = resolve(PrimerPaymentMethodManagerCategory.NOL_PAY.name),
                        errorLoggingDelegate = resolve(name = PaymentMethodType.NOL_PAY.name),
                        validationErrorLoggingDelegate = resolve(PaymentMethodType.NOL_PAY.name),
                        validatorRegistry = resolve(),
                        errorMapperRegistry = resolve(),
                        savedStateHandle = extras.createSavedStateHandle()
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
