package io.primer.android.payment.apaya

import io.primer.android.PaymentMethod
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class ApayaFactory(private val settings: PrimerSettings) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        return Success(
            Apaya(
                settings.paymentMethodOptions.apayaOptions.webViewTitle
                    ?: "Pay by mobile"
            )
        )
    }
}
