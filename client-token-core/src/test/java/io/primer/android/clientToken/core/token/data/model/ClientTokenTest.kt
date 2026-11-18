package io.primer.android.clientToken.core.token.data.model

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ClientTokenTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), Base64.URL_SAFE) } answers {
            java.util.Base64.getUrlDecoder().decode(firstArg<String>())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `fromString accepts a token whose endpoints are Primer hosts`() {
        val token =
            ClientToken.fromString(
                encode(
                    configurationUrl = "https://api.sandbox.primer.io/client-sdk/configuration",
                    analyticsUrlV2 = "https://analytics.sandbox.data.primer.io/sdk-logs",
                ),
            )

        assertEquals("https://api.sandbox.primer.io/client-sdk/configuration", token.configurationUrl)
        assertEquals("https://analytics.sandbox.data.primer.io/sdk-logs", token.analyticsUrlV2)
        assertEquals("CHECKOUT", token.intent)
        assertEquals("access", token.accessToken)
    }

    @Test
    fun `fromString accepts a token without endpoints`() {
        val token = ClientToken.fromString(encode(configurationUrl = null, analyticsUrlV2 = null))

        assertNull(token.configurationUrl)
        assertNull(token.analyticsUrlV2)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://attacker.example/client-sdk/configuration",
            "http://api.sandbox.primer.io/client-sdk/configuration",
            "https://api.sandbox.primer.io.attacker.example/client-sdk/configuration",
        ],
    )
    fun `fromString rejects a token whose configurationUrl is not a Primer host`(configurationUrl: String) {
        assertThrows<InvalidClientTokenException> {
            ClientToken.fromString(
                encode(
                    configurationUrl = configurationUrl,
                    analyticsUrlV2 = "https://analytics.sandbox.data.primer.io/sdk-logs",
                ),
            )
        }
    }

    @Test
    fun `fromString rejects a token whose analyticsUrlV2 is not a Primer host`() {
        assertThrows<InvalidClientTokenException> {
            ClientToken.fromString(
                encode(
                    configurationUrl = "https://api.sandbox.primer.io/client-sdk/configuration",
                    analyticsUrlV2 = "https://attacker.example/sdk-logs",
                ),
            )
        }
    }

    @Test
    fun `fromString rejects an expired token before looking at its endpoints`() {
        assertThrows<ExpiredClientTokenException> {
            ClientToken.fromString(
                encode(
                    configurationUrl = "https://attacker.example/client-sdk/configuration",
                    analyticsUrlV2 = null,
                    exp = 1,
                ),
            )
        }
    }

    @Test
    fun `fromString rejects a blank token`() {
        assertThrows<InvalidClientTokenException> { ClientToken.fromString(" ") }
    }

    private fun encode(
        configurationUrl: String?,
        analyticsUrlV2: String?,
        exp: Long = System.currentTimeMillis() / 1000 + 3600,
    ): String {
        val payload =
            JSONObject().apply {
                put("exp", exp)
                put("accessToken", "access")
                put("intent", "CHECKOUT")
                configurationUrl?.let { put("configurationUrl", it) }
                analyticsUrlV2?.let { put("analyticsUrlV2", it) }
            }
        val encoder = java.util.Base64.getUrlEncoder().withoutPadding()
        return listOf("""{"alg":"HS256","typ":"JWT"}""", payload.toString(), "signature")
            .joinToString(".") { encoder.encodeToString(it.toByteArray()) }
    }
}
