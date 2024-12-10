package io.primer.android.nolpay.api.manager.payment.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.nolpay.api.manager.payment.component.NolPayPaymentComponent
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor
import io.primer.android.nolpay.implementation.common.presentation.BaseNolPayDelegate
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

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
                    val paymentMethodType = PaymentMethodType.NOL_PAY.name
                    return NolPayPaymentComponent(
                        baseNolPayDelegate = object : BaseNolPayDelegate {
                            override val sdkInitInteractor: NolPaySdkInitInteractor
                                get() = resolve()
                        },
                        tokenizationDelegate = resolve(),
                        paymentDelegate = resolve(),
                        eventLoggingDelegate = resolve(name = paymentMethodType),
                        errorLoggingDelegate = resolve(name = PrimerPaymentMethodManagerCategory.NOL_PAY.name),
                        validationErrorLoggingDelegate = resolve(PaymentMethodType.NOL_PAY.name),
                        validatorRegistry = resolve(),
                        errorMapperRegistry = resolve()
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
