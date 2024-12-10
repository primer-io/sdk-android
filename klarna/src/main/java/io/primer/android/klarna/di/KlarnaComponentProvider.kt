package io.primer.android.klarna.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class KlarnaComponentProvider : DISdkComponent {

    fun provideInstance(
        owner: ViewModelStoreOwner,
        primerSessionIntent: PrimerSessionIntent
    ): KlarnaComponent {
        return ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    val paymentMethodType = PaymentMethodType.KLARNA.name
                    return KlarnaComponent(
                        tokenizationDelegate = resolve(name = paymentMethodType),
                        paymentDelegate = resolve(name = paymentMethodType),
                        klarnaSessionCreationDelegate = resolve(),
                        mockConfigurationDelegate = resolve(),
                        eventLoggingDelegate = resolve(name = paymentMethodType),
                        errorLoggingDelegate = resolve(name = paymentMethodType),
                        validationErrorLoggingDelegate = resolve(name = paymentMethodType),
                        authorizationSessionDataDelegate = resolve(),
                        errorMapperRegistry = resolve(),
                        createKlarnaPaymentView = { context, paymentCategory, callback, returnUrl ->
                            KlarnaPaymentView(
                                context = context,
                                category = paymentCategory,
                                callback = callback,
                                returnURL = returnUrl
                            )
                        },
                        primerSettings = resolve(),
                        primerSessionIntent = primerSessionIntent
                    ) as T
                }
            }
        )[KlarnaComponent::class.java]
    }
}
