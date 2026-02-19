package io.primer.cardShared.binData.data.model

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.sequence
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import org.json.JSONObject

data class BinDataItemResponse(
    val network: CardNetwork.Type?,
    val displayName: String,
    val issuerCountryCode: String,
    val issuerName: String?,
    val accountFundingType: String,
    val prepaidReloadableIndicator: String,
    val productUsageType: String,
    val productCode: String,
    val productName: String,
    val issuerCurrencyCode: String?,
    val regionalRestriction: String,
    val accountNumberType: String,
) : JSONDeserializable {
    companion object {
        const val NETWORK_FIELD = "network"
        const val DISPLAY_NAME_FIELD = "displayName"
        const val ISSUER_COUNTRY_CODE_FIELD = "issuerCountryCode"
        const val ISSUER_NAME_FIELD = "issuerName"
        const val ACCOUNT_FUNDING_TYPE_FIELD = "accountFundingType"
        const val PREPAID_RELOADABLE_INDICATOR_FIELD = "prepaidReloadableIndicator"
        const val PRODUCT_USAGE_TYPE_FIELD = "productUsageType"
        const val PRODUCT_CODE_FIELD = "productCode"
        const val PRODUCT_NAME_FIELD = "productName"
        const val ISSUER_CURRENCY_CODE_FIELD = "issuerCurrencyCode"
        const val REGIONAL_RESTRICTION_FIELD = "regionalRestriction"
        const val ACCOUNT_NUMBER_TYPE_FIELD = "accountNumberType"

        @JvmField
        val deserializer =
            JSONObjectDeserializer {
                BinDataItemResponse(
                    network = CardNetwork.Type.valueOrNull(it.getString(NETWORK_FIELD)),
                    displayName = it.getString(DISPLAY_NAME_FIELD),
                    issuerCountryCode = it.getString(ISSUER_COUNTRY_CODE_FIELD),
                    issuerName = it.optNullableString(ISSUER_NAME_FIELD),
                    accountFundingType = it.getString(ACCOUNT_FUNDING_TYPE_FIELD),
                    prepaidReloadableIndicator = it.getString(PREPAID_RELOADABLE_INDICATOR_FIELD),
                    productUsageType = it.getString(PRODUCT_USAGE_TYPE_FIELD),
                    productCode = it.getString(PRODUCT_CODE_FIELD),
                    productName = it.getString(PRODUCT_NAME_FIELD),
                    issuerCurrencyCode = it.optNullableString(ISSUER_CURRENCY_CODE_FIELD),
                    regionalRestriction = it.getString(REGIONAL_RESTRICTION_FIELD),
                    accountNumberType = it.getString(ACCOUNT_NUMBER_TYPE_FIELD),
                )
            }
    }
}

data class BinDataResponse(
    val firstDigits: String,
    val binData: List<BinDataItemResponse>,
) : JSONDeserializable {
    companion object {
        internal const val FIRST_DIGITS_FIELD = "firstDigits"
        internal const val BIN_DATA_FIELD = "binData"

        @JvmField
        val deserializer =
            JSONObjectDeserializer {
                BinDataResponse(
                    firstDigits = it.getString(FIRST_DIGITS_FIELD),
                    binData = it.getJSONArray(BIN_DATA_FIELD).sequence<JSONObject>()
                        .map { binDataItem ->
                            JSONSerializationUtils
                                .getJsonObjectDeserializer<BinDataItemResponse>()
                                .deserialize(binDataItem)
                        }.toList(),
                )
            }

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
                        primitiveKey(FIRST_DIGITS_FIELD)
                        nonPrimitiveKey(BIN_DATA_FIELD) {
                            primitiveKey(BinDataItemResponse.NETWORK_FIELD)
                            primitiveKey(BinDataItemResponse.DISPLAY_NAME_FIELD)
                            primitiveKey(BinDataItemResponse.ISSUER_COUNTRY_CODE_FIELD)
                            primitiveKey(BinDataItemResponse.ISSUER_NAME_FIELD)
                            primitiveKey(BinDataItemResponse.ACCOUNT_FUNDING_TYPE_FIELD)
                            primitiveKey(BinDataItemResponse.PREPAID_RELOADABLE_INDICATOR_FIELD)
                            primitiveKey(BinDataItemResponse.PRODUCT_USAGE_TYPE_FIELD)
                            primitiveKey(BinDataItemResponse.PRODUCT_CODE_FIELD)
                            primitiveKey(BinDataItemResponse.PRODUCT_NAME_FIELD)
                            primitiveKey(BinDataItemResponse.ISSUER_CURRENCY_CODE_FIELD)
                            primitiveKey(BinDataItemResponse.REGIONAL_RESTRICTION_FIELD)
                            primitiveKey(BinDataItemResponse.ACCOUNT_NUMBER_TYPE_FIELD)
                        }
                    }
            }
    }
}
