package io.primer.android.components.currencyformat.domain.repository

import io.primer.android.components.currencyformat.domain.models.CurrencyFormat

interface CurrencyFormatRepository {
    suspend fun fetchCurrencyFormats(): Result<Unit>

    fun getCurrencyFormats(): List<CurrencyFormat>
}
