package io.primer.android.domain.currencyformat.interactors

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.base.BaseInteractor
import io.primer.android.domain.currencyformat.models.FormatCurrencyParams
import io.primer.android.domain.currencyformat.repository.CurrencyFormatRepository
import io.primer.android.utils.PaymentUtils.minorToAmount
import java.text.NumberFormat
import java.util.Currency

internal class FormatAmountToCurrencyInteractor(
    private val currencyFormatRepository: CurrencyFormatRepository,
    private val settings: PrimerSettings
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
