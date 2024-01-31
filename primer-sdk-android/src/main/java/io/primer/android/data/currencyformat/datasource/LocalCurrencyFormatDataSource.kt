package io.primer.android.data.currencyformat.datasource

import android.content.Context
import io.primer.android.R
import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.currencyformat.exception.MissingCurrencyFormatsException
import io.primer.android.data.currencyformat.models.CurrencyFormatDataResponse
import org.json.JSONArray

internal class LocalCurrencyFormatDataSource(private val context: Context) :
    BaseCacheDataSource<CurrencyFormatDataResponse, CurrencyFormatDataResponse> {

    private var data: CurrencyFormatDataResponse? = null
    private val fallbackData by lazy { loadCurrencyFormats() }

    override fun get(): CurrencyFormatDataResponse {
        return data ?: fallbackData
    }

    override fun update(input: CurrencyFormatDataResponse) {
        this.data = input
    }

    private fun loadCurrencyFormats(): CurrencyFormatDataResponse {
        return try {
            val dataJson = context.resources?.openRawResource(R.raw.currency_formats)
                ?.readBytes()
                ?.decodeToString().orEmpty()
            CurrencyFormatDataResponse.deserializer.deserialize(JSONArray(dataJson))
        } catch (e: Exception) {
            throw MissingCurrencyFormatsException(e)
        }
    }
}
