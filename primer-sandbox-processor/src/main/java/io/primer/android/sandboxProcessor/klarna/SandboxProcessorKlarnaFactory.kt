package io.primer.android.sandboxProcessor.klarna

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class SandboxProcessorKlarnaFactory(private val type: String) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(SandboxProcessorKlarna(paymentMethodType = type))
    }
}
