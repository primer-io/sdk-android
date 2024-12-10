package io.primer.android.qrcode.implementation.composer.ui.assets

import io.primer.android.offsession.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PromptPayBrandTest {

    private lateinit var promptPayBrand: PromptPayBrand

    @BeforeEach
    fun setUp() {
        promptPayBrand = PromptPayBrand()
    }

    @Test
    fun `should return correct resource IDs`() {
        assertEquals(R.drawable.ic_logo_promptpay_dark, promptPayBrand.iconResId)
        assertEquals(R.drawable.ic_logo_promptpay_light, promptPayBrand.iconLightResId)
    }
}
