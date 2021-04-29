package io.primer.android.payment.google

import android.content.Context
import io.primer.android.PaymentMethodModule
import io.primer.android.model.dto.ClientSession
import io.primer.android.model.dto.Environment
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

class GooglePayModule(
    private val googlePayFacadeFactory: GooglePayFacadeFactory = GooglePayFacadeFactory(),
) : PaymentMethodModule {

    private lateinit var googlePayFacade: GooglePayFacade

    override fun initialize(applicationContext: Context, clientSession: ClientSession) {
        val googlePayEnvironment = if (clientSession.environment == Environment.PRODUCTION) {
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
            GOOGLE_PAY_IDENTIFIER,
            googlePayChecker
        )
    }

    override fun registerPaymentMethodDescriptorFactory(
        paymentMethodDescriptorFactoryRegistry: PaymentMethodDescriptorFactoryRegistry,
    ) {
        val paymentMethodDescriptorFactory =
            GooglePayPaymentMethodDescriptorFactory(googlePayFacade)

        paymentMethodDescriptorFactoryRegistry.register(
            GOOGLE_PAY_IDENTIFIER,
            paymentMethodDescriptorFactory
        )
    }
}
