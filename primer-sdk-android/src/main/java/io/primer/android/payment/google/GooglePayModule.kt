package io.primer.android.payment.google

import android.content.Context
import io.primer.android.PaymentMethodModule
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.GooglePayPaymentMethodChecker
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

class GooglePayModule(
    private val googlePayFacadeFactory: GooglePayFacadeFactory = GooglePayFacadeFactory(),
) : PaymentMethodModule {

    private lateinit var googlePayFacade: GooglePayFacade

    override fun initialize(applicationContext: Context) {
        googlePayFacade = googlePayFacadeFactory.create(applicationContext)
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
