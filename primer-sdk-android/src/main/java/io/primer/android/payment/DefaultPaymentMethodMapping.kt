package io.primer.android.payment

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.payment.apaya.ApayaFactory
import io.primer.android.payment.card.CardFactory
import io.primer.android.payment.gocardless.GoCardlessFactory
import io.primer.android.payment.google.GooglePayFactory
import io.primer.android.payment.klarna.KlarnaFactory
import io.primer.android.payment.paypal.PayPalFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure

internal interface PaymentMethodMapping {

    fun getPaymentMethodFor(type: String): Either<PaymentMethod, Exception>
}

internal class DefaultPaymentMethodMapping(val settings: PrimerSettings) : PaymentMethodMapping {

    override fun getPaymentMethodFor(type: String): Either<PaymentMethod, Exception> =
        when (type) {
            PAYMENT_CARD_IDENTIFIER -> CardFactory().build()
            KLARNA_IDENTIFIER -> KlarnaFactory(settings).build()
            GOOGLE_PAY_IDENTIFIER -> GooglePayFactory(settings).build()
            PAYPAL_IDENTIFIER -> PayPalFactory().build()
            GOCARDLESS_IDENTIFIER -> GoCardlessFactory(settings).build()
            APAYA_IDENTIFIER -> ApayaFactory(settings).build()
            else -> Failure(Exception("Unknown payment method, can't register."))
        }
}
