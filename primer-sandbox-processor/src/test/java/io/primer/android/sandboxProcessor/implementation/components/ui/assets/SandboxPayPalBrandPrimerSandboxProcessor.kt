package io.primer.android.sandboxProcessor.implementation.components.ui.assets

import io.mockk.MockKAnnotations
import io.primer.android.sandboxProcessor.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SandboxPayPalBrandPrimerSandboxProcessor {
    private lateinit var testPayPalBrand: SandboxProcessorPayPalBrand

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testPayPalBrand = SandboxProcessorPayPalBrand()
    }

    @Test
    fun `iconResId should return correct PayPal icon resource`() {
        assertEquals(R.drawable.ic_logo_paypal, testPayPalBrand.iconResId)
    }

    @Test
    fun `logoResId should return correct PayPal logo resource`() {
        assertEquals(R.drawable.ic_logo_paypal_square, testPayPalBrand.logoResId)
    }
}
