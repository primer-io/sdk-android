package io.primer.android.core.data.network.utils

import io.primer.android.core.data.network.exception.InvalidUrlException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PrimerUrlTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://primer.io",
            "https://api.sandbox.primer.io/client-sdk/configuration",
            "https://api.staging.primer.io/client-sdk/configuration",
            "https://api.primer.io/client-sdk/configuration?withDisplayMetadata=true",
            "https://analytics.production.data.primer.io/sdk-logs",
            "https://PRIMER.IO/path",
        ],
    )
    fun `accepts https urls on primer io and its subdomains`(url: String) {
        assertTrue(url.isPrimerUrl())
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
            "not a url",
            "http://api.sandbox.primer.io/client-sdk/configuration",
            "https://attacker.example/client-sdk/configuration",
            "https://evil-primer.io/client-sdk/configuration",
            "https://primer.io.attacker.example/client-sdk/configuration",
            "https://api.primer.io@attacker.example/client-sdk/configuration",
            "https://attacker.example/?redirect=https://api.primer.io",
            "https://attacker.example#api.primer.io",
            "https://xprimer.io",
        ],
    )
    fun `rejects everything else`(url: String) {
        assertFalse(url.isPrimerUrl())
    }

    @Test
    fun `requirePrimerUrl returns a Primer url unchanged`() {
        val url = "https://api.sandbox.primer.io/resume-tokens/id"

        assertEquals(url, url.requirePrimerUrl())
    }

    @Test
    fun `requirePrimerUrl throws InvalidUrlException for any other url`() {
        val url = "https://attacker.example/resume-tokens/id"

        val exception = assertThrows<InvalidUrlException> { url.requirePrimerUrl() }

        assertEquals(url, exception.url)
    }
}
