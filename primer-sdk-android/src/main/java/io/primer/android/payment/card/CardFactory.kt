package io.primer.android.payment.card

import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class CardFactory : PaymentMethodFactory() {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(Card())
    }
}
