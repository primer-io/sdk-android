package io.primer.android.model.dto

import io.primer.android.PaymentMethod
import io.primer.android.PaymentMethodIntent
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.card.Card
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import kotlinx.serialization.Serializable

@Serializable
internal data class PrimerIntent(
    val paymentMethodIntent: PaymentMethodIntent = PaymentMethodIntent.CHECKOUT,
    val paymentMethod: PrimerPaymentMethod = PrimerPaymentMethod.ANY,
) {

    companion object {

        fun build(vaulted: Boolean, paymentMethods: List<PaymentMethod>): PrimerIntent {
            val sessionIntent =
                if (vaulted) PaymentMethodIntent.VAULT else PaymentMethodIntent.CHECKOUT

            if (paymentMethods.size > 1) {
                return PrimerIntent(sessionIntent, PrimerPaymentMethod.ANY)
            }

            if (paymentMethods.isEmpty()) return PrimerIntent(
                sessionIntent,
                PrimerPaymentMethod.CARD
            )

            return when (paymentMethods.first()) {
                is Klarna -> PrimerIntent(sessionIntent, PrimerPaymentMethod.KLARNA)
                is Card -> PrimerIntent(sessionIntent, PrimerPaymentMethod.CARD)
                is PayPal -> PrimerIntent(sessionIntent, PrimerPaymentMethod.PAYPAL)
                is GooglePay -> PrimerIntent(sessionIntent, PrimerPaymentMethod.GOOGLE_PAY)
                is GoCardless -> PrimerIntent(sessionIntent, PrimerPaymentMethod.GOCARDLESS)
                is Apaya -> PrimerIntent(sessionIntent, PrimerPaymentMethod.APAYA)
                else -> PrimerIntent(sessionIntent, PrimerPaymentMethod.CARD)
            }
        }
    }
}

@Serializable
enum class PrimerPaymentMethod {

    ANY,
    CARD,
    KLARNA,
    PAYPAL,
    GOOGLE_PAY,
    GOCARDLESS,
    APAYA,
    ATOME,
    PAY_NL_IDEAL,
    PAY_NL_PAYCONIQ,
    PAY_NL_GIROPAY,
    HOOLAH,
    ADYEN_GIROPAY,
    ADYEN_TWINT,
    ADYEN_SOFORT,
    ADYEN_TRUSTLY,
    ADYEN_ALIPAY,
    ADYEN_VIPPS,
    ADYEN_MOBILEPAY,
    ADYEN_INTERAC,
    ADYEN_PAYTRAIL,
    ADYEN_PAYSHOP,
    MOLLIE_BANCONTACT,
    MOLLIE_IDEAL,
    BUCKAROO_GIROPAY,
    BUCKAROO_SOFORT,
    BUCKAROO_IDEAL,
    BUCKAROO_EPS,
    BUCKAROO_BANCONTACT,
    COINBASE,
    TWOC2P,
    OPENNODE,
}
