package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.utils.Either

internal abstract class PaymentMethodFactory {

    abstract fun build(): Either<PaymentMethod, Exception>
}
