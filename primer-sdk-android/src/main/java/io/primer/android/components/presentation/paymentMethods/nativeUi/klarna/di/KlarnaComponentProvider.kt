package io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.klarna.mobile.sdk.api.payments.KlarnaPaymentView
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.nativeUi.klarna.composable.KlarnaComponent
import io.primer.android.di.DISdkComponent
import io.primer.android.di.KlarnaContainer.Companion.KLARNA_ERROR_RESOLVER_NAME
import io.primer.android.di.extension.resolve

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
                    return KlarnaComponent(
                        klarnaTokenizationDelegate = resolve(),
                        klarnaSessionCreationDelegate = resolve(),
                        headlessManagerDelegate = resolve(),
                        mockConfigurationDelegate = resolve(),
                        eventLoggingDelegate = resolve(
                            PrimerPaymentMethodManagerCategory.NATIVE_UI.name
                        ),
                        validationErrorLoggingDelegate = resolve(),
                        errorLoggingDelegate = resolve(),
                        authorizationSessionDataDelegate = resolve(),
                        errorEventResolver = resolve(),
                        errorMapper = resolve(KLARNA_ERROR_RESOLVER_NAME),
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
