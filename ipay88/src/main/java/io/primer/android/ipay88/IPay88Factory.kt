package io.primer.android.ipay88

import io.primer.android.core.utils.Either
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.android.core.utils.Success

class IPay88Factory(val type: String) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(IPay88PaymentMethod(type))
    }
}
