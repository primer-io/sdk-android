package io.primer.android.di

import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.PaymentMethodSdkAnalyticsEventLoggingDelegate
import io.primer.android.components.presentation.paymentMethods.analytics.delegate.SdkAnalyticsErrorLoggingDelegate

internal class LoggingContainer(private val sdk: SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton { SdkAnalyticsErrorLoggingDelegate(analyticsInteractor = sdk.resolve()) }

        registerSingleton(PrimerPaymentMethodManagerCategory.NOL_PAY.name) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory = PrimerPaymentMethodManagerCategory.NOL_PAY,
                analyticsInteractor = sdk.resolve()
            )
        }

        registerSingleton(PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT.name) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT,
                analyticsInteractor = sdk.resolve()
            )
        }

        registerSingleton(PrimerPaymentMethodManagerCategory.NATIVE_UI.name) {
            PaymentMethodSdkAnalyticsEventLoggingDelegate(
                primerPaymentMethodManagerCategory =
                PrimerPaymentMethodManagerCategory.NATIVE_UI,
                analyticsInteractor = sdk.resolve()
            )
        }
    }
}
