package io.primer.android.sandboxProcessor.paypal

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class SandboxProcessorPayPalFactory(private val type: String) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        return Success(SandboxProcessorPayPal(paymentMethodType = type))
    }
}
