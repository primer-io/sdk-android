package io.primer.android.payment.klarna

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class KlarnaFactory(val settings: PrimerSettings) : PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val klarna = Klarna(
            settings.order.description,
            settings.options.klarnaWebViewTitle ?: "Klarna"
        )

        return Success(klarna)
    }
}
