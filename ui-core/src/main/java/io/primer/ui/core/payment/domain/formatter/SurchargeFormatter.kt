package io.primer.ui.core.payment.domain.formatter

import io.primer.android.configuration.domain.model.Surcharge
import java.text.NumberFormat
import java.util.Currency

interface SurchargeFormatter {
    fun formatSurchargeAsString(
        amount: Int,
        currency: Currency,
        excludeZero: Boolean = true,
        noFeeText: String = "No additional fee"
    ): String

    fun getSurchargeLabelText(
        amount: Int?,
        currency: Currency,
        mayApplyText: String = "Additional fees may apply"
    ): String

    fun formatSurchargeAmount(surcharge: Surcharge, currency: Currency): String
}

class DefaultSurchargeFormatter : SurchargeFormatter {

    override fun formatSurchargeAsString(
        amount: Int,
        currency: Currency,
        excludeZero: Boolean,
        noFeeText: String
    ): String {
        if (amount == 0 && excludeZero) return noFeeText
        
        val numberFormat = NumberFormat.getCurrencyInstance().apply {
            this.currency = currency
        }
        val formattedAmount = numberFormat.format(amount / 100.0)
        return "+$formattedAmount"
    }

    override fun getSurchargeLabelText(
        amount: Int?,
        currency: Currency,
        mayApplyText: String
    ): String {
        return if (amount == null) {
            mayApplyText
        } else {
            formatSurchargeAsString(amount, currency)
        }
    }

    override fun formatSurchargeAmount(surcharge: Surcharge, currency: Currency): String {
        return when (surcharge) {
            is Surcharge.PaymentMethodSurcharge -> {
                formatSurchargeAsString(surcharge.amount, currency)
            }
            is Surcharge.CardNetworksSurcharge -> {
                "Additional fees may apply"
            }
        }
    }
}