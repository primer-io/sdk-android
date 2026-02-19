package io.primer.cardShared.binData.data.model

import io.primer.android.configuration.data.model.CardNetwork
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BinDataItemResponseTest {
    @Test
    fun `deserialization works correctly with all fields`() {
        val json =
            JSONObject().apply {
                put(BinDataItemResponse.NETWORK_FIELD, "VISA")
                put(BinDataItemResponse.DISPLAY_NAME_FIELD, "Visa")
                put(BinDataItemResponse.ISSUER_COUNTRY_CODE_FIELD, "US")
                put(BinDataItemResponse.ISSUER_NAME_FIELD, "Chase")
                put(BinDataItemResponse.ACCOUNT_FUNDING_TYPE_FIELD, "CREDIT")
                put(BinDataItemResponse.PREPAID_RELOADABLE_INDICATOR_FIELD, "NOT_APPLICABLE")
                put(BinDataItemResponse.PRODUCT_USAGE_TYPE_FIELD, "CONSUMER")
                put(BinDataItemResponse.PRODUCT_CODE_FIELD, "A")
                put(BinDataItemResponse.PRODUCT_NAME_FIELD, "Visa Classic")
                put(BinDataItemResponse.ISSUER_CURRENCY_CODE_FIELD, "USD")
                put(BinDataItemResponse.REGIONAL_RESTRICTION_FIELD, "NONE")
                put(BinDataItemResponse.ACCOUNT_NUMBER_TYPE_FIELD, "PRIMARY_ACCOUNT_NUMBER")
            }

        val result = BinDataItemResponse.deserializer.deserialize(json)

        assertEquals(
            BinDataItemResponse(
                network = CardNetwork.Type.VISA,
                displayName = "Visa",
                issuerCountryCode = "US",
                issuerName = "Chase",
                accountFundingType = "CREDIT",
                prepaidReloadableIndicator = "NOT_APPLICABLE",
                productUsageType = "CONSUMER",
                productCode = "A",
                productName = "Visa Classic",
                issuerCurrencyCode = "USD",
                regionalRestriction = "NONE",
                accountNumberType = "PRIMARY_ACCOUNT_NUMBER",
            ),
            result,
        )
    }

    @Test
    fun `deserialization works correctly with nullable fields missing`() {
        val json =
            JSONObject().apply {
                put(BinDataItemResponse.NETWORK_FIELD, "MASTERCARD")
                put(BinDataItemResponse.DISPLAY_NAME_FIELD, "Mastercard")
                put(BinDataItemResponse.ISSUER_COUNTRY_CODE_FIELD, "GB")
                put(BinDataItemResponse.ACCOUNT_FUNDING_TYPE_FIELD, "DEBIT")
                put(BinDataItemResponse.PREPAID_RELOADABLE_INDICATOR_FIELD, "NOT_APPLICABLE")
                put(BinDataItemResponse.PRODUCT_USAGE_TYPE_FIELD, "CONSUMER")
                put(BinDataItemResponse.PRODUCT_CODE_FIELD, "MCC")
                put(BinDataItemResponse.PRODUCT_NAME_FIELD, "Mastercard Credit")
                put(BinDataItemResponse.REGIONAL_RESTRICTION_FIELD, "NONE")
                put(BinDataItemResponse.ACCOUNT_NUMBER_TYPE_FIELD, "PRIMARY_ACCOUNT_NUMBER")
            }

        val result = BinDataItemResponse.deserializer.deserialize(json)

        assertEquals("Mastercard", result.displayName)
        assertEquals(CardNetwork.Type.MASTERCARD, result.network)
        assertNull(result.issuerName)
        assertNull(result.issuerCurrencyCode)
    }
}

class BinDataResponseTest {
    @Test
    fun `deserialization works correctly`() {
        val itemJson =
            JSONObject().apply {
                put(BinDataItemResponse.NETWORK_FIELD, "VISA")
                put(BinDataItemResponse.DISPLAY_NAME_FIELD, "Visa")
                put(BinDataItemResponse.ISSUER_COUNTRY_CODE_FIELD, "US")
                put(BinDataItemResponse.ACCOUNT_FUNDING_TYPE_FIELD, "CREDIT")
                put(BinDataItemResponse.PREPAID_RELOADABLE_INDICATOR_FIELD, "NOT_APPLICABLE")
                put(BinDataItemResponse.PRODUCT_USAGE_TYPE_FIELD, "CONSUMER")
                put(BinDataItemResponse.PRODUCT_CODE_FIELD, "A")
                put(BinDataItemResponse.PRODUCT_NAME_FIELD, "Visa Classic")
                put(BinDataItemResponse.REGIONAL_RESTRICTION_FIELD, "NONE")
                put(BinDataItemResponse.ACCOUNT_NUMBER_TYPE_FIELD, "PRIMARY_ACCOUNT_NUMBER")
            }
        val json =
            JSONObject().apply {
                put(BinDataResponse.FIRST_DIGITS_FIELD, "40355000")
                put(BinDataResponse.BIN_DATA_FIELD, JSONArray().apply { put(itemJson) })
            }

        val result = BinDataResponse.deserializer.deserialize(json)

        assertEquals("40355000", result.firstDigits)
        assertEquals(1, result.binData.size)
        assertEquals(CardNetwork.Type.VISA, result.binData[0].network)
        assertEquals("Visa", result.binData[0].displayName)
    }

    @Test
    fun `deserialization works correctly with empty binData`() {
        val json =
            JSONObject().apply {
                put(BinDataResponse.FIRST_DIGITS_FIELD, "40355000")
                put(BinDataResponse.BIN_DATA_FIELD, JSONArray())
            }

        val result = BinDataResponse.deserializer.deserialize(json)

        assertEquals("40355000", result.firstDigits)
        assertEquals(0, result.binData.size)
    }
}
