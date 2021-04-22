package io.primer.android.payment.google

import io.primer.android.GooglePay
import io.primer.android.PaymentMethod
import io.primer.android.model.dto.CheckoutConfig
import io.primer.android.model.dto.PaymentMethodRemoteConfig
import io.primer.android.payment.GOOGLE_PAY_IDENTIFIER
import io.primer.android.payment.PaymentMethodDescriptorFactory
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry

internal class GooglePayPaymentMethodDescriptorFactory(
    private val googlePayBridge: GooglePayBridge,
) : PaymentMethodDescriptorFactory {

    override fun create(
        checkoutConfig: CheckoutConfig,
        paymentMethodRemoteConfig: PaymentMethodRemoteConfig,
        paymentMethod: PaymentMethod,
        paymentMethodCheckers: PaymentMethodCheckerRegistry,
    ): GooglePayDescriptor =
        GooglePayDescriptor(
            checkoutConfig = checkoutConfig,
            options = paymentMethod as GooglePay,
            paymentMethodChecker = paymentMethodCheckers[GOOGLE_PAY_IDENTIFIER]
                ?: throw Error("Missing payment method checker"),
            googlePayBridge = googlePayBridge,
            config = paymentMethodRemoteConfig
        )
}
