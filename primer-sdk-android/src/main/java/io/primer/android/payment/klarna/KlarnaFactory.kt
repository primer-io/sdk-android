package io.primer.android.payment.klarna

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class KlarnaFactory(private val type: PaymentMethodType, val settings: PrimerSettings) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val klarnaOptions = settings.paymentMethodOptions.klarnaOptions
        val klarna = Klarna(
            type,
            klarnaOptions.recurringPaymentDescription ?: settings.order.description,
            klarnaOptions.webViewTitle ?: "Klarna"
        )

        return Success(klarna)
    }
}
