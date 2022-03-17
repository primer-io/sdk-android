package io.primer.android.payment

import android.content.Context
import androidx.annotation.DrawableRes
import io.primer.android.R
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal

internal abstract class TokenAttributes private constructor(
    token: PaymentMethodTokenInternal,
    @DrawableRes val icon: Int,
    val iconScale: Float = 1.0f,
) {

    val id = token.token

    protected val data = token.paymentInstrumentData

    abstract fun getDescription(context: Context): String

    internal class PaymentCardAttributes(token: PaymentMethodTokenInternal) :
        TokenAttributes(token, R.drawable.ic_logo_credit_card) {

        override fun getDescription(context: Context): String {
            val network = data?.network ?: context.getString(R.string.card_network_fallback)
            val digits = data?.last4Digits.toString() ?: ""
            var description = network

            if (digits.isNotEmpty()) {
                description += " ●●●●$digits"
            }

            return description.trim()
        }
    }

    internal class PayPalBillingAgreementAttributes(token: PaymentMethodTokenInternal) :
        TokenAttributes(token, R.drawable.icon_paypal_sm) {

        override fun getDescription(context: Context): String =
            data?.externalPayerInfo?.email ?: "PayPal"
    }

    internal class GoCardlessMandateAttributes(token: PaymentMethodTokenInternal) :
        TokenAttributes(token, R.drawable.ic_logo_gocardless) {

        override fun getDescription(context: Context): String {
            val ref = data?.gocardlessMandateId ?: ""
            return context.getString(R.string.bank_account) + " $ref"
        }
    }

    companion object {

        fun create(token: PaymentMethodTokenInternal): TokenAttributes? {
            // FIXME this is tightly coupled to each payment type
            return when (token.paymentInstrumentType) {
                PAYMENT_CARD_TYPE -> PaymentCardAttributes(token)
                PAYPAL_BILLING_AGREEMENT_TYPE -> PayPalBillingAgreementAttributes(token)
                GOCARDLESS_MANDATE_TYPE -> GoCardlessMandateAttributes(token)
                else -> null
            }
        }
    }
}
