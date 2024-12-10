package io.primer.android.clientToken.core.validation.data.model

import io.mockk.every
import io.mockk.mockk
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

internal class TokenCheckStatusDataResponseTest {

    @Test
    fun `deserializer should return TokenCheckStatusDataResponse with success true`() {
        // Arrange
        val jsonObject = mockk<JSONObject>()
        every { jsonObject.optBoolean("success") } returns true

        // Act
        val result = TokenCheckStatusDataResponse.deserializer.deserialize(jsonObject)

        // Assert
        assertEquals(true, result.success)
    }

    @Test
    fun `deserializer should return TokenCheckStatusDataResponse with success false`() {
        // Arrange
        val jsonObject = mockk<JSONObject>()
        every { jsonObject.optBoolean("success") } returns false

        // Act
        val result = TokenCheckStatusDataResponse.deserializer.deserialize(jsonObject)

        // Assert
        assertEquals(false, result.success)
    }

    @Test
    fun `deserializer should return TokenCheckStatusDataResponse with success null`() {
        // Arrange
        val jsonObject = mockk<JSONObject>()
        every { jsonObject.optBoolean("success") } returns false

        // Act
        val result = TokenCheckStatusDataResponse.deserializer.deserialize(jsonObject)

        // Assert
        assertFalse(result.success ?: false)
    }
}
