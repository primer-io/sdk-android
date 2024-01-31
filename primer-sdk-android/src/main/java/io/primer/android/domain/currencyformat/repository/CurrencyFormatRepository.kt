package io.primer.android.domain.currencyformat.repository

import io.primer.android.domain.currencyformat.models.CurrencyFormat

internal interface CurrencyFormatRepository {

    suspend fun fetchCurrencyFormats(): Result<Unit>

    fun getCurrencyFormats(): List<CurrencyFormat>
}
