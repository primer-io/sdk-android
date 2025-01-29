package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CreateSessionDataResponseTest {
    private val createSessionDataResponse by lazy {
        CreateSessionDataResponse.deserializer.deserialize(
            JSONObject(JSON_OBJECT),
        )
    }

    @Test
    fun `provider should correctly whitelist keys`() {
        // Given
        val expectedKeys =
            setOf(
                WhitelistedKey.PrimitiveWhitelistedKey("sessionId"),
                WhitelistedKey.NonPrimitiveWhitelistedKey(
                    value = "categories",
                    children =
                    listOf(
                        WhitelistedKey.PrimitiveWhitelistedKey("identifier"),
                        WhitelistedKey.PrimitiveWhitelistedKey("name"),
                        WhitelistedKey.PrimitiveWhitelistedKey("descriptiveAssetUrl"),
                        WhitelistedKey.PrimitiveWhitelistedKey("standardAssetUrl"),
                    ),
                ),
            )

        // When
        val actualKeys = CreateSessionDataResponse.provider.values.toSet()

        println(actualKeys)
        println(expectedKeys)
        // Then
        assertTrue(expectedKeys == actualKeys)
    }

    @Test
    fun `createSessionDataResponse should be deserialized correctly`() {
        val klarnaSession =
            KlarnaSession(
                SESSION_ID,
                CLIENT_TOKEN,
                listOf(
                    KlarnaPaymentCategory(
                        identifier = CATEGORY_IDENTIFIER,
                        name = CATEGORY_NAME,
                        descriptiveAssetUrl = CATEGORY_DESCRIPTIVE_ASSET_URL,
                        standardAssetUrl = CATEGORY_STANDARD_ASSET_URL,
                    ),
                ),
            )

        assertEquals(klarnaSession, createSessionDataResponse.toKlarnaSession())
    }

    private companion object {
        const val SESSION_ID = "ea59c16b-18e9-436e-bc99"
        const val CLIENT_TOKEN = "test.token"
        const val CATEGORY_IDENTIFIER = "pay_over_time"
        const val CATEGORY_NAME = "Monthly financing"
        const val CATEGORY_DESCRIPTIVE_ASSET_URL =
            "https://assets.staging.core.primer.io/OVO/ovo-logo-dark@3x.png"
        const val CATEGORY_STANDARD_ASSET_URL =
            "https://assets.staging.core.primer.io/OVO/ovo-logo-dark@3x.png"

        const val JSON_OBJECT =
            """
        {       
          "clientToken": "$CLIENT_TOKEN",
          "sessionId": "$SESSION_ID",
          "categories": [
            {
                "identifier": "pay_over_time",
                "name": "Monthly financing",
                "descriptiveAssetUrl": "$CATEGORY_DESCRIPTIVE_ASSET_URL",
                "standardAssetUrl": "$CATEGORY_STANDARD_ASSET_URL"
            }
          ],
          "hppRedirectUrl": null
        }
            """
    }
}
