package io.primer.android.payment.klarna

import io.primer.android.PaymentMethod
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.methods.mapping.PaymentMethodFactory
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payment.klarna.helpers.KlarnaSdkClassValidator
import io.primer.android.utils.Either
import io.primer.android.utils.Failure
import io.primer.android.utils.Success

internal class KlarnaFactory(private val type: String, val settings: PrimerSettings) :
    PaymentMethodFactory {

    override fun build(): Either<PaymentMethod, Exception> {
        val klarnaOptions = settings.paymentMethodOptions.klarnaOptions
        val klarna = Klarna(
            type,
            klarnaOptions.recurringPaymentDescription ?: settings.order.description,
            klarnaOptions.webViewTitle ?: "Klarna"
        )

        if (type == PaymentMethodType.PRIMER_TEST_KLARNA.name) {
            return Success(klarna)
        }

        if (KlarnaSdkClassValidator().isKlarnaSdkIncluded().not()) {
            return Failure(
                IllegalStateException(
                    KlarnaSdkClassValidator.KLARNA_CLASS_NOT_LOADED_ERROR
                )
            )
        }

        return Success(klarna)
    }
}
