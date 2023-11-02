package io.primer.android.payment.google

import android.content.Context
import io.primer.android.PaymentMethodModule
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.Environment
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.di.DISdkComponent
import io.primer.android.di.extension.resolve
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class GooglePayModule(
    private val googlePayFacadeFactory: GooglePayFacadeFactory = GooglePayFacadeFactory()
) : PaymentMethodModule, DISdkComponent {

    private lateinit var googlePayFacade: GooglePayFacade

    override fun initialize(applicationContext: Context, configuration: ConfigurationData) {
        val googlePayEnvironment = if (configuration.environment == Environment.PRODUCTION) {
            GooglePayFacade.Environment.PRODUCTION
        } else {
            GooglePayFacade.Environment.TEST
        }
        googlePayFacade =
            googlePayFacadeFactory.create(applicationContext, googlePayEnvironment, resolve())
    }

    override fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry
    ) {
        val googlePayChecker = GooglePayPaymentMethodChecker(googlePayFacade)

        paymentMethodCheckerRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            googlePayChecker
        )
    }

    override fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry
    ) {
        val paymentMethodDescriptorFactory =
            GooglePayPaymentMethodDescriptorFactory()

        paymentMethodDescriptorFactoryRegistry.register(
            PaymentMethodType.GOOGLE_PAY.name,
            paymentMethodDescriptorFactory
        )
    }
}
