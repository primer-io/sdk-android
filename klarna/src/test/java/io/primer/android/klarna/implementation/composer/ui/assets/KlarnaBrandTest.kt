package io.primer.android.klarna.implementation.composer.ui.assets

import io.primer.android.klarna.main.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KlarnaBrandTest {
    private lateinit var klarnaBrand: KlarnaBrand

    @BeforeEach
    fun setUp() {
        klarnaBrand = KlarnaBrand()
    }

    @Test
    fun `iconResId should return correct resource ID`() {
        val expectedResId = R.drawable.ic_logo_klarna
        assertEquals(expectedResId, klarnaBrand.iconResId)
    }

    @Test
    fun `logoResId should return correct resource ID`() {
        val expectedResId = R.drawable.ic_logo_klarna_square
        assertEquals(expectedResId, klarnaBrand.logoResId)
    }

    @Test
    fun `iconDarkResId should return correct resource ID`() {
        val expectedResId = R.drawable.ic_logo_klarna_dark
        assertEquals(expectedResId, klarnaBrand.iconDarkResId)
    }
}
