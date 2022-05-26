package io.primer.android.payment.paypal

import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class PayPalFactory(private val type: PaymentMethodType) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(PayPal(type))
    }
}
