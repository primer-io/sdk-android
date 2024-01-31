package io.primer.android.utils

import android.content.Context
import io.primer.android.R
import io.primer.android.data.base.models.BasePaymentToken
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.currencyformat.interactors.FormatAmountToCurrencyInteractor
import io.primer.android.domain.currencyformat.models.FormatCurrencyParams
import io.primer.android.model.MonetaryAmount

internal class SurchargeFormatter(
    private val actionInteractor: ActionInteractor,
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val currency: java.util.Currency
) {

    fun getSurchargeForSavedPaymentMethod(token: BasePaymentToken?): Int {
        if (token == null) return 0
        val type = token.paymentMethodType
        return if (type == PaymentMethodType.PAYMENT_CARD.name) {
            actionInteractor.surcharges[token.paymentInstrumentData?.binData?.network] ?: 0
        } else {
            actionInteractor.surcharges[type] ?: 0
        }
    }

    fun getSurchargeForPaymentMethodType(type: String, network: String? = null): Int =
        if (type != PaymentMethodType.PAYMENT_CARD.name) {
            actionInteractor.surcharges[type] ?: 0
        } else {
            actionInteractor.surcharges[network] ?: 0
        }

    fun formatSurchargeAsString(
        amount: Int,
        excludeZero: Boolean = true,
        context: Context
    ): String {
        if (amount == 0 && excludeZero) return context.getString(R.string.no_additional_fee)
        val monetaryAmount = MonetaryAmount.create(currency.currencyCode, amount)
        return monetaryAmount?.let {
            "+" + amountToCurrencyInteractor.execute(
                params = FormatCurrencyParams(monetaryAmount)
            )
        } ?: "+"
    }

    fun getSurchargeLabelTextForPaymentMethodType(amount: Int?, context: Context): String =
        if (amount == null) {
            context.getString(R.string.additional_fees_may_apply)
        } else {
            formatSurchargeAsString(amount, context = context)
        }
}
