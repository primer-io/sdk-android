package io.primer.android.data.payments.methods.mapping

import io.primer.android.PaymentMethod
import io.primer.android.utils.Either

internal abstract class PaymentMethodFactory {

    abstract fun build(): Either<PaymentMethod, Exception>
}
