package io.primer.android.bancontact

import io.primer.android.core.utils.Either
import io.primer.android.core.utils.Success
import io.primer.android.paymentmethods.PaymentMethod
import io.primer.android.paymentmethods.PaymentMethodFactory

class AdyenBancontactFactory : PaymentMethodFactory {
    override fun build(): Either<PaymentMethod, Exception> {
        return Success(AdyenBancontact())
    }
}
