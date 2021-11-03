package io.primer.android.payment.google

import android.content.Context
import io.primer.android.PaymentMethodModule
import io.primer.android.data.configuration.model.Configuration
import io.primer.android.data.configuration.model.Environment
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

class GooglePayModule(
    private val googlePayFacadeFactory: GooglePayFacadeFactory = GooglePayFacadeFactory(),
) : PaymentMethodModule {

    private lateinit var googlePayFacade: GooglePayFacade

    override fun initialize(applicationContext: Context, configuration: Configuration) {
        val googlePayEnvironment = if (configuration.environment == Environment.PRODUCTION) {
            GooglePayFacade.Environment.PRODUCTION
        } else {
            GooglePayFacade.Environment.TEST
        }
        googlePayFacade = googlePayFacadeFactory.create(applicationContext, googlePayEnvironment)
    }

    override fun registerPaymentMethodCheckers(
        paymentMethodCheckerRegistry: PaymentMethodCheckerRegistry,
    ) {
        val googlePayChecker = GooglePayPaymentMethodChecker(googlePayFacade)

        paymentMethodCheckerRegistry.register(
            PaymentMethodType.GOOGLE_PAY,
            googlePayChecker
        )
    }

    override fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    ) {
        val paymentMethodDescriptorFactory =
            GooglePayPaymentMethodDescriptorFactory(googlePayFacade)

        paymentMethodDescriptorFactoryRegistry.register(
            PaymentMethodType.GOOGLE_PAY,
            paymentMethodDescriptorFactory
        )
    }
}
