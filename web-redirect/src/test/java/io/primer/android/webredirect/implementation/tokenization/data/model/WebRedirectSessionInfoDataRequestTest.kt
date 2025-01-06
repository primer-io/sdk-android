package io.primer.android.webredirect.implementation.tokenization.data.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebRedirectSessionInfoDataRequestTest {
    @Test
    fun `serializer should correctly serialize WebRedirectSessionInfoDataRequest to JSONObject`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val platform = "ANDROID"
        val request =
            WebRedirectSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                platform = platform,
            )

        // Act
        val jsonObject = WebRedirectSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
    }

    @Test
    fun `serializer should set default platform value if not provided`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val request =
            WebRedirectSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
            )

        // Act
        val jsonObject = WebRedirectSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals("ANDROID", jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
    }

    @Test
    fun `serializer should correctly handle different platform value`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val platform = "IOS"
        val request =
            WebRedirectSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                platform = platform,
            )

        // Act
        val jsonObject = WebRedirectSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
    }
}
