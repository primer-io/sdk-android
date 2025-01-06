package io.primer.android.components.currencyformat.data.datasource

import android.content.Context
import io.primer.android.components.currencyformat.data.exception.MissingCurrencyFormatsException
import io.primer.android.components.currencyformat.data.models.CurrencyFormatDataResponse
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.headlessCore.R
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
            val dataJson =
                context.resources?.openRawResource(R.raw.currency_formats)
                    ?.readBytes()
                    ?.decodeToString().orEmpty()
            CurrencyFormatDataResponse.deserializer.deserialize(JSONArray(dataJson))
        } catch (e: Exception) {
            throw MissingCurrencyFormatsException(e)
        }
    }
}
