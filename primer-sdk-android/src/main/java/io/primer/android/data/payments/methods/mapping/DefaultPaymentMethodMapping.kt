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
            PaymentMethodType.PAY_NL_IDEAL,
            PaymentMethodType.PAY_NL_PAYCONIQ,
            PaymentMethodType.PAY_NL_GIROPAY,
            PaymentMethodType.PAY_NL_P24,
            PaymentMethodType.PAY_NL_EPS,
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
            PaymentMethodType.ADYEN_BLIK,
//            PaymentMethodType.ADYEN_MBWAY,
//            PaymentMethodType.ADYEN_BANK_TRANSFER,
            PaymentMethodType.ADYEN_INTERAC,
            PaymentMethodType.ADYEN_PAYTRAIL,
            PaymentMethodType.ADYEN_PAYSHOP,
            PaymentMethodType.MOLLIE_BANCONTACT,
            PaymentMethodType.MOLLIE_IDEAL,
            PaymentMethodType.MOLLIE_P24,
            PaymentMethodType.MOLLIE_GIROPAY,
            PaymentMethodType.MOLLIE_EPS,
            PaymentMethodType.BUCKAROO_GIROPAY,
            PaymentMethodType.BUCKAROO_SOFORT,
            PaymentMethodType.BUCKAROO_IDEAL,
            PaymentMethodType.BUCKAROO_EPS,
            PaymentMethodType.BUCKAROO_BANCONTACT,
            PaymentMethodType.ATOME,
            PaymentMethodType.XFERS_PAYNOW
            -> AsyncMethodFactory(
                type,
                settings
            ).build()
            PaymentMethodType.APAYA -> ApayaFactory(settings).build()
            else -> Failure(Exception("Unknown payment method, can't register."))
        }
}
