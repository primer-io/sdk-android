package io.primer.android.threeds.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale
import java.util.stream.Stream

internal class LocaleTest {

    @Test
    fun `should return locale with language and country`() {
        val locale = Locale("en", "US")
        val result = locale.toNormalizedLocale()
        assertEquals("en_US", result)
    }

    @Test
    fun `should return only language if country is missing`() {
        val locale = Locale("fr")
        val result = locale.toNormalizedLocale()
        assertEquals("fr", result)
    }

    @Test
    fun `should strip extensions`() {
        val locale = Locale.Builder().setLanguage("en").setRegion("US").setExtension('u', "ca-gregory").build()
        val result = locale.toNormalizedLocale()
        assertEquals("en_US", result)
    }

    @Test
    fun `should remove script`() {
        val locale = Locale.Builder().setLanguage("zh").setRegion("HK").setScript("Hant").build()
        val result = locale.toNormalizedLocale()
        assertEquals("zh_HK", result)
    }

    @Test
    fun `should format language tags properly`() {
        val locale = Locale("fr_FR", "FR")
        val result = locale.toNormalizedLocale()
        assertEquals("fr_FR", result)
    }

    @Test
    fun `should handle multiple extensions`() {
        val locale = Locale.Builder().setLanguage("en")
            .setRegion("GB")
            .setExtension('u', "ca-buddhist")
            .setExtension('t', "und-latn")
            .build()
        val result = locale.toNormalizedLocale()
        assertEquals("en_GB", result)
    }

    @ParameterizedTest
    @MethodSource("malformedLocaleProvider")
    fun `should handle invalid languages`(locale: Locale, expected: String) {
        val result = locale.toNormalizedLocale()
        assertEquals(expected, result)
    }

    private companion object {
        @JvmStatic
        fun malformedLocaleProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(Locale("pl-pl", "PL"), "pl_PL"),
            Arguments.of(Locale("zh-hk", "HK"), "zh_HK"),
            Arguments.of(Locale("de-at", "AT"), "de_AT"),
            Arguments.of(Locale("tr-cy", "CY"), "tr_CY"),
        )
    }
}
