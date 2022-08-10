package io.primer.android.payment.async

import io.primer.android.PaymentMethod
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class AsyncMethodFactory(val type: String, val settings: PrimerSettings) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(AsyncPaymentMethod(type))
    }
}
