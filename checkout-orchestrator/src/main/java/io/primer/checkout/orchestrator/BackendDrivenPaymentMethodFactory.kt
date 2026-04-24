package io.primer.checkout.orchestrator

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class BackendDrivenPaymentMethodFactory(
    private val type: String,
) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> = Success(BackendDrivenPaymentMethod(type = type))
}
