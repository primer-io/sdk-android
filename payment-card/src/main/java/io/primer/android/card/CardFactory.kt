package io.primer.android.card

import io.primer.android.core.utils.Either
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory
import io.primer.android.core.utils.Success

class CardFactory : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(Card())
    }
}
