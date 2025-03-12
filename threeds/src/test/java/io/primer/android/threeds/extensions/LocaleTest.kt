package io.primer.android.threeds.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

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
    fun `should handle multiple extensions`() {
        val locale = Locale.Builder().setLanguage("en")
            .setRegion("GB")
            .setExtension('u', "ca-buddhist")
            .setExtension('t', "und-latn")
            .build()
        val result = locale.toNormalizedLocale()
        assertEquals("en_GB", result)
    }
}
