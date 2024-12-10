package io.primer.android.paypal

import io.primer.android.core.utils.Either
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.android.core.utils.Success

class PayPalFactory(private val type: String) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(PayPal(type))
    }
}
