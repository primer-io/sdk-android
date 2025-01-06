package io.primer.android.banks.implementation.tokenization.data.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BankIssuerSessionInfoDataRequestTest {
    @Test
    fun `serializer should correctly serialize BankIssuerSessionInfoDataRequest to JSONObject`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val issuer = "issuer"
        val locale = "en-US"
        val platform = "ANDROID"
        val request =
            BankIssuerSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                platform = platform,
                issuer = issuer,
            )

        // Act
        val jsonObject = BankIssuerSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
        assertEquals(issuer, jsonObject.getString("issuer"))
    }

    @Test
    fun `serializer should set default platform value if not provided`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val issuer = "issuer"
        val request =
            BankIssuerSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                issuer = issuer,
            )

        // Act
        val jsonObject = BankIssuerSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals("ANDROID", jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
        assertEquals(issuer, jsonObject.getString("issuer"))
    }

    @Test
    fun `serializer should correctly handle different platform value`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val platform = "IOS"
        val issuer = "issuer"
        val request =
            BankIssuerSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                platform = platform,
                issuer = issuer,
            )

        // Act
        val jsonObject = BankIssuerSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
        assertEquals(issuer, jsonObject.getString("issuer"))
    }
}
