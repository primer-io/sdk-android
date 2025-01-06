package io.primer.android.card.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.card.implementation.payment.resume.clientToken.data.model.CardNative3DSClientTokenData
import io.primer.android.card.implementation.payment.resume.clientToken.data.model.CardProcessor3dsClientTokenData
import io.primer.android.card.implementation.payment.resume.clientToken.domain.model.Card3DSClientToken
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.paymentmethods.common.data.model.ClientTokenIntent
import io.primer.android.processor3ds.domain.model.Processor3DS
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class CardNativeClientTokenParserTest {
    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(ClientToken)
        mockkObject(CardNative3DSClientTokenData)
        mockkObject(CardProcessor3dsClientTokenData)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseClientToken should correctly parse when called with a valid Native 3DS client token`() {
        // Given
        every { ClientToken.fromString(validEncodedString) } returns
            mockk<ClientToken> {
                every { this@mockk.intent } returns ClientTokenIntent.`3DS_AUTHENTICATION`.name
            }
        every { CardNative3DSClientTokenData.fromString(validEncodedString) } returns
            CardNative3DSClientTokenData("testIntent", listOf("1.0", "2.0"))

        val parser = CardNative3DSClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected =
            Card3DSClientToken.CardNative3DSClientToken(
                "testIntent",
                listOf("1.0", "2.0"),
            )
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should correctly parse when called with a valid Processor 3DS client token`() {
        // Given
        every { ClientToken.fromString(validEncodedString) } returns
            mockk<ClientToken> {
                every { this@mockk.intent } returns ClientTokenIntent.PROCESSOR_3DS.name
            }
        every { CardProcessor3dsClientTokenData.fromString(validEncodedString) } returns
            CardProcessor3dsClientTokenData(
                intent = "testIntent",
                statusUrl = "https://www.example.com/status",
                redirectUrl = "https://www.example.com/redirect",
            )

        val parser = CardNative3DSClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected =
            Card3DSClientToken.CardProcessor3DSClientToken(
                clientTokenIntent = "testIntent",
                processor3DS =
                    Processor3DS(
                        statusUrl = "https://www.example.com/status",
                        redirectUrl = "https://www.example.com/redirect",
                    ),
            )
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { ClientToken.fromString(validEncodedString) } returns
            mockk<ClientToken> {
                every { this@mockk.intent } returns ClientTokenIntent.`3DS_AUTHENTICATION`.name
            }
        every { CardNative3DSClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()
        every { CardProcessor3dsClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = CardNative3DSClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}
