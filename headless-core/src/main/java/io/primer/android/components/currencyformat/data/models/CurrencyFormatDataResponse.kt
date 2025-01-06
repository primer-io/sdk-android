package io.primer.android.components.currencyformat.data.models

import io.primer.android.components.currencyformat.domain.models.CurrencyFormat
import io.primer.android.core.data.serialization.json.JSONArrayDeserializer
import io.primer.android.core.data.serialization.json.JSONDeserializable

internal data class CurrencyFormatDataResponse(val data: List<CurrencyFormat>) :
    JSONDeserializable {
    companion object {
        private const val CODE_FIELD = "c"
        private const val DECIMAL_DIGITS_FIELD = "m"

        @JvmField
        val deserializer =
            JSONArrayDeserializer { jsonArray ->
                val dataList =
                    (0 until jsonArray.length()).asSequence().map {
                        CurrencyFormat(
                            jsonArray.getJSONObject(it).getString(CODE_FIELD),
                            jsonArray.getJSONObject(it).getInt(DECIMAL_DIGITS_FIELD),
                        )
                    }.toList()
                return@JSONArrayDeserializer CurrencyFormatDataResponse(data = dataList)
            }
    }
}
