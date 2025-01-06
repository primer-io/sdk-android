package io.primer.android.paymentmethods

import io.primer.android.core.utils.Either

interface PaymentMethodFactory {
    fun build(): Either<PaymentMethod, Exception>
}
