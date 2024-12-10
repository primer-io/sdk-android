package io.primer.android.otp.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.otp.implementation.payment.resume.clientToken.data.model.OtpClientTokenData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OtpClientTokenParserTest {
    @Test
    fun `parseClientToken should correctly parse client token`() {
        // Arrange
        val clientToken = "clientToken"
        val mockOtpClientTokenData = OtpClientTokenData(intent = "intent", statusUrl = "statusUrl")
        mockkObject(OtpClientTokenData)
        every { OtpClientTokenData.fromString(clientToken) } returns mockOtpClientTokenData

        val otpClientTokenParser = OtpClientTokenParser()

        // Act
        val result = otpClientTokenParser.parseClientToken(clientToken)

        // Assert
        assertEquals("intent", result.clientTokenIntent)
        assertEquals("statusUrl", result.statusUrl)
        verify { OtpClientTokenData.fromString(clientToken) }
        unmockkObject(OtpClientTokenData::class)
    }
}
