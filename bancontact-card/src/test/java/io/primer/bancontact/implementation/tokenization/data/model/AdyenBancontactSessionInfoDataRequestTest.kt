package io.primer.bancontact.implementation.tokenization.data.model

import io.primer.android.bancontact.implementation.tokenization.data.model.AdyenBancontactSessionInfoDataRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdyenBancontactSessionInfoDataRequestTest {
    @Test
    fun `serializer should correctly serialize AdyenBancontactSessionInfoDataRequest to JSONObject`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val userAgent = "userAgent"
        val locale = "en-US"
        val platform = "ANDROID"
        val request =
            AdyenBancontactSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                platform = platform,
                userAgent = "userAgent",
            )

        // Act
        val jsonObject = AdyenBancontactSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
        assertEquals(userAgent, jsonObject.getJSONObject("browserInfo").getString(userAgent))
    }

    @Test
    fun `serializer should set default platform value if not provided`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val userAgent = "userAgent"
        val request =
            AdyenBancontactSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                userAgent = userAgent,
            )

        // Act
        val jsonObject = AdyenBancontactSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals("ANDROID", jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
        assertEquals(userAgent, jsonObject.getJSONObject("browserInfo").getString(userAgent))
    }

    @Test
    fun `serializer should correctly handle different platform value`() {
        // Arrange
        val redirectionUrl = "https://example.com"
        val locale = "en-US"
        val platform = "IOS"
        val userAgent = "userAgent"
        val request =
            AdyenBancontactSessionInfoDataRequest(
                redirectionUrl = redirectionUrl,
                locale = locale,
                platform = platform,
                userAgent = userAgent,
            )

        // Act
        val jsonObject = AdyenBancontactSessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
        assertEquals(redirectionUrl, jsonObject.getString("redirectionUrl"))
        assertEquals(userAgent, jsonObject.getJSONObject("browserInfo").getString(userAgent))
    }
}
