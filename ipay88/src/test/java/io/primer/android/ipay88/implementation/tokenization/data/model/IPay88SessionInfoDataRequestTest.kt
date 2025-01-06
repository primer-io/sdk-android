package io.primer.android.ipay88.implementation.tokenization.data.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IPay88SessionInfoDataRequestTest {
    @Test
    fun `serializer should correctly serialize IPay88SessionInfoDataRequest to JSONObject`() {
        // Arrange
        val locale = "en-US"
        val platform = "ANDROID"
        val request =
            IPay88SessionInfoDataRequest(
                locale = locale,
                platform = platform,
            )

        // Act
        val jsonObject = IPay88SessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
    }

    @Test
    fun `serializer should set default platform value if not provided`() {
        // Arrange
        val locale = "en-US"
        val request =
            IPay88SessionInfoDataRequest(
                locale = locale,
            )

        // Act
        val jsonObject = IPay88SessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals("ANDROID", jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
    }

    @Test
    fun `serializer should correctly handle different platform value`() {
        // Arrange
        val locale = "en-US"
        val platform = "IOS"
        val request =
            IPay88SessionInfoDataRequest(
                locale = locale,
                platform = platform,
            )

        // Act
        val jsonObject = IPay88SessionInfoDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(platform, jsonObject.getString("platform"))
        assertEquals(locale, jsonObject.getString("locale"))
    }
}
