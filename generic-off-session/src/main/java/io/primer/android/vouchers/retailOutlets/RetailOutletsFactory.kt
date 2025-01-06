package io.primer.android.vouchers.retailOutlets

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class RetailOutletsFactory(val paymentMethodType: String) : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        return Success(RetailOutlets(paymentMethodType))
    }
}
