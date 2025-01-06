package io.primer.android.surcharge.utils

import android.content.Context
import io.primer.android.R
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.core.domain.None
import io.primer.android.currencyformat.domain.FormatAmountToCurrencyInteractor
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.surcharge.domain.SurchargeInteractor
import java.util.Currency

internal class SurchargeFormatter(
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val surchargeInteractor: SurchargeInteractor,
    private val currency: Currency,
) {
    fun getSurchargeForSavedPaymentMethod(token: PrimerVaultedPaymentMethod?): Int {
        if (token == null) return 0
        val type = token.paymentMethodType
        return if (type == PaymentMethodType.PAYMENT_CARD.name) {
            surchargeInteractor(None)[token.paymentInstrumentData.binData?.network] ?: 0
        } else {
            surchargeInteractor(None)[type] ?: 0
        }
    }

    fun getSurchargeForPaymentMethodType(
        type: String,
        network: String? = null,
    ): Int =
        if (type != PaymentMethodType.PAYMENT_CARD.name) {
            surchargeInteractor(None)[type] ?: 0
        } else {
            surchargeInteractor(None)[network] ?: 0
        }

    fun formatSurchargeAsString(
        amount: Int,
        excludeZero: Boolean = true,
        context: Context,
    ): String {
        if (amount == 0 && excludeZero) return context.getString(R.string.no_additional_fee)
        val monetaryAmount = MonetaryAmount.create(currency.currencyCode, amount)
        return monetaryAmount?.let {
            "+" +
                amountToCurrencyInteractor.execute(
                    params = FormatCurrencyParams(monetaryAmount),
                )
        } ?: "+"
    }

    fun getSurchargeLabelTextForPaymentMethodType(
        amount: Int?,
        context: Context,
    ): String =
        if (amount == null) {
            context.getString(R.string.additional_fees_may_apply)
        } else {
            formatSurchargeAsString(amount, context = context)
        }
}
