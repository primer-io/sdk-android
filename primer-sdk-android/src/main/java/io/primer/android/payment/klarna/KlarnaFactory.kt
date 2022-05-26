package io.primer.android.payment.klarna

import io.primer.android.PaymentMethod
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.utils.Either
import io.primer.android.utils.Success

internal class KlarnaFactory(private val type: PaymentMethodType, val settings: PrimerSettings) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val klarna = Klarna(
            type,
            settings.order.description,
            settings.order.items,
            settings.options.klarnaWebViewTitle ?: "Klarna"
        )

        return Success(klarna)
    }
}
