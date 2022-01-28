package io.primer.android.data.payments.methods.mapping

import io.primer.android.PaymentMethod
import io.primer.android.utils.Either

internal interface PaymentMethodFactory {

    fun build(): Either<PaymentMethod, Exception>
}
