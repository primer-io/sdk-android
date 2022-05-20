package io.primer.android.data.settings.internal

import io.primer.android.PaymentMethod
import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.payment.apaya.Apaya
import io.primer.android.payment.card.Card
import io.primer.android.payment.gocardless.GoCardless
import io.primer.android.payment.google.GooglePay
import io.primer.android.payment.klarna.Klarna
import io.primer.android.payment.paypal.PayPal
import kotlinx.serialization.Serializable

@Serializable
internal data class PrimerIntent(
    val paymentMethodIntent: PrimerPaymentMethodIntent = PrimerPaymentMethodIntent.CHECKOUT,
    val paymentMethod: PrimerPaymentMethod = PrimerPaymentMethod.ANY,
) {

    companion object {

        fun build(vaulted: Boolean, paymentMethods: List<PaymentMethod>): PrimerIntent {
            val sessionIntent =
                if (vaulted) PrimerPaymentMethodIntent.VAULT else PrimerPaymentMethodIntent.CHECKOUT

            if (paymentMethods.size > 1) {
                return PrimerIntent(sessionIntent, PrimerPaymentMethod.ANY)
            }

            if (paymentMethods.isEmpty()) return PrimerIntent(
                sessionIntent,
                PrimerPaymentMethod.PAYMENT_CARD
            )

            return when (paymentMethods.first()) {
                is Klarna -> PrimerIntent(sessionIntent, PrimerPaymentMethod.KLARNA)
                is Card -> PrimerIntent(sessionIntent, PrimerPaymentMethod.PAYMENT_CARD)
                is PayPal -> PrimerIntent(sessionIntent, PrimerPaymentMethod.PAYPAL)
                is GooglePay -> PrimerIntent(sessionIntent, PrimerPaymentMethod.GOOGLE_PAY)
                is GoCardless -> PrimerIntent(sessionIntent, PrimerPaymentMethod.GOCARDLESS)
                is Apaya -> PrimerIntent(sessionIntent, PrimerPaymentMethod.APAYA)
                else -> PrimerIntent(sessionIntent, PrimerPaymentMethod.PAYMENT_CARD)
            }
        }
    }
}

@Serializable
enum class PrimerPaymentMethod {

    ANY,
    PAYMENT_CARD,
    KLARNA,
    PAYPAL,
    GOOGLE_PAY,
    GOCARDLESS,
    APAYA,
    ATOME,
    PAY_NL_IDEAL,
    PAY_NL_PAYCONIQ,
    PAY_NL_GIROPAY,
    PAY_NL_EPS,
    PAY_NL_P24,
    HOOLAH,
    ADYEN_GIROPAY,
    ADYEN_TWINT,
    ADYEN_SOFORT,
    ADYEN_TRUSTLY,
    ADYEN_ALIPAY,
    ADYEN_VIPPS,
    ADYEN_MOBILEPAY,
    ADYEN_PAYTRAIL,
    ADYEN_INTERAC,
    MOLLIE_BANCONTACT,
    MOLLIE_IDEAL,
    MOLLIE_P24,
    MOLLIE_GIROPAY,
    MOLLIE_EPS,
    BUCKAROO_GIROPAY,
    BUCKAROO_SOFORT,
    BUCKAROO_IDEAL,
    BUCKAROO_EPS,
    BUCKAROO_BANCONTACT,
}
