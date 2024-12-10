package io.primer.android.vouchers.multibanco.implementation.composer.ui.assets

import io.primer.android.offsession.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MultibancoBrandTest {

    private lateinit var multibancoBrand: MultibancoBrand

    @BeforeEach
    fun setUp() {
        multibancoBrand = MultibancoBrand()
    }

    @Test
    fun `should return correct resource IDs`() {
        assertEquals(R.drawable.ic_logo_multibanco_dark, multibancoBrand.iconResId)
        assertEquals(R.drawable.ic_logo_multibanco_light, multibancoBrand.iconLightResId)
    }
}
