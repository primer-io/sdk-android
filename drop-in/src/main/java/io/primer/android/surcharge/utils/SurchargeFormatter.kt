package io.primer.android.surcharge.utils

import android.content.Context
import io.primer.android.R
import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.core.domain.None
import io.primer.android.currencyformat.domain.FormatAmountToCurrencyInteractor
import io.primer.android.data.settings.internal.MonetaryAmount
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.surcharge.domain.SurchargeInteractor
import io.primer.ui.core.payment.domain.interactor.SurchargeCalculationInteractor
import java.util.Currency

internal class SurchargeFormatter(
    private val amountToCurrencyInteractor: FormatAmountToCurrencyInteractor,
    private val surchargeInteractor: SurchargeInteractor,
    private val currency: Currency,
) {
    private val surchargeCalculationInteractor = SurchargeCalculationInteractor()

    fun getSurchargeForSavedPaymentMethod(token: PrimerVaultedPaymentMethod?): Int {
        return surchargeCalculationInteractor.getSurchargeForSavedPaymentMethod(
            token = token,
            surcharges = surchargeInteractor(None)
        )
    }

    fun getSurchargeForPaymentMethodType(
        type: String,
        network: String? = null,
    ): Int {
        return surchargeCalculationInteractor.getSurchargeForPaymentMethodType(
            type = type,
            network = network,
            surcharges = surchargeInteractor(None)
        )
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
