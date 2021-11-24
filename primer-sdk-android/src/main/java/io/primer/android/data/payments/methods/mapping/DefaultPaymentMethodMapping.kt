package io.primer.android.data.payments.methods.mapping

import io.primer.android.PaymentMethod
import io.primer.android.model.dto.PaymentMethodType
import io.primer.android.model.dto.PrimerSettings
import io.primer.android.payment.apaya.ApayaFactory
import io.primer.android.payment.async.AsyncMethodFactory
import io.primer.android.payment.card.CardFactory
import io.primer.android.payment.gocardless.GoCardlessFactory
import io.primer.android.payment.google.GooglePayFactory
import io.primer.android.payment.klarna.KlarnaFactory
import io.primer.android.payment.paypal.PayPalFactory
import io.primer.android.utils.Either
import io.primer.android.utils.Failure

internal interface PaymentMethodMapping {

    fun getPaymentMethodFor(type: PaymentMethodType): Either<PaymentMethod, Exception>
}

internal class DefaultPaymentMethodMapping(val settings: PrimerSettings) : PaymentMethodMapping {

    override fun getPaymentMethodFor(type: PaymentMethodType): Either<PaymentMethod, Exception> =
        when (type) {
            PaymentMethodType.PAYMENT_CARD -> CardFactory().build()
            PaymentMethodType.KLARNA -> KlarnaFactory(settings).build()
            PaymentMethodType.GOOGLE_PAY -> GooglePayFactory(settings).build()
            PaymentMethodType.PAYPAL -> PayPalFactory().build()
            PaymentMethodType.GOCARDLESS -> GoCardlessFactory(settings).build()
            PaymentMethodType.APAYA -> ApayaFactory(settings).build()
            PaymentMethodType.PAY_NL_IDEAL,
            PaymentMethodType.PAY_NL_PAYCONIQ,
            PaymentMethodType.PAY_NL_GIROPAY,
            PaymentMethodType.HOOLAH,
            PaymentMethodType.ADYEN_GIROPAY,
            PaymentMethodType.ADYEN_TWINT,
            PaymentMethodType.ADYEN_SOFORT,
            PaymentMethodType.ADYEN_TRUSTLY,
            PaymentMethodType.ADYEN_ALIPAY,
            PaymentMethodType.ADYEN_VIPPS,
            PaymentMethodType.ADYEN_MOBILEPAY,
            PaymentMethodType.ADYEN_IDEAL,
            PaymentMethodType.ADYEN_DOTPAY,
            PaymentMethodType.MOLLIE_BANCONTACT,
            PaymentMethodType.MOLLIE_IDEAL,
            -> AsyncMethodFactory(
                type,
                settings
            ).build()
            else -> Failure(Exception("Unknown payment method, can't register."))
        }
}
