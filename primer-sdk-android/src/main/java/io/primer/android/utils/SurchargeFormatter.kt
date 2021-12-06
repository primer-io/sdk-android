package io.primer.android.utils

import android.content.Context
import io.primer.android.R
import io.primer.android.model.dto.MonetaryAmount
import io.primer.android.model.dto.PaymentMethodTokenInternal

internal class SurchargeFormatter(
    private val surcharges: Map<String, Int>,
    private val currency: java.util.Currency,
) {

    fun getSurchargeForSavedPaymentMethod(token: PaymentMethodTokenInternal?): Int {
        if (token == null) return 0
        val isBillingAgreement = token.paymentInstrumentType == "PAYPAL_BILLING_AGREEMENT"
        val type = if (isBillingAgreement) "PAYPAL" else token.paymentInstrumentType

        return if (type == "PAYMENT_CARD") {
            surcharges[token.paymentInstrumentData?.binData?.network] ?: 0
        } else {
            surcharges[type] ?: 0
        }
    }

    fun getSurchargeForPaymentMethodType(type: String, network: String? = null): Int =
        if (type != "PAYMENT_CARD") surcharges[type] ?: 0
        else surcharges[network] ?: 0

    fun formatSurchargeAsString(
        amount: Int,
        excludeZero: Boolean = true,
        context: Context,
    ): String {
        if (amount == 0 && excludeZero) return context.getString(R.string.no_additional_fee)
        val monetaryAmount = MonetaryAmount.create(currency.currencyCode, amount)
        return "+" + PaymentUtils.amountToCurrencyString(monetaryAmount)
    }

    fun getSurchargeLabelTextForPaymentMethodType(amount: Int?, context: Context): String =
        if (amount == null) context.getString(R.string.additional_fees_may_apply)
        else formatSurchargeAsString(amount, context = context)
}
