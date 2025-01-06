package io.primer.android.currencyformat.domain

import io.primer.android.components.currencyformat.domain.models.FormatCurrencyParams
import io.primer.android.components.currencyformat.domain.repository.CurrencyFormatRepository
import io.primer.android.core.domain.BaseInteractor
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.payments.core.utils.PaymentUtils.minorToAmount
import java.text.NumberFormat
import java.util.Currency

internal class FormatAmountToCurrencyInteractor(
    private val currencyFormatRepository: CurrencyFormatRepository,
    private val settings: PrimerSettings,
) : BaseInteractor<String, FormatCurrencyParams>() {
    override fun execute(params: FormatCurrencyParams): String {
        val amount = params.amount
        val formats = currencyFormatRepository.getCurrencyFormats()
        val currencyFormat = formats.first { it.code == amount.currency }
        val formatter = NumberFormat.getCurrencyInstance(settings.locale)

        formatter.currency = Currency.getInstance(currencyFormat.code)
        formatter.maximumFractionDigits = currencyFormat.fractionDigits
        formatter.minimumFractionDigits = currencyFormat.fractionDigits

        return formatter.format(minorToAmount(amount.value, currencyFormat.fractionDigits))
    }
}
