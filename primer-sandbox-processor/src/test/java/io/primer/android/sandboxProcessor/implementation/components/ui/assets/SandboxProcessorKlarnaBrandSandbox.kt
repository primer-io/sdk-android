package io.primer.android.sandboxProcessor.implementation.components.ui.assets

import io.mockk.MockKAnnotations
import io.primer.android.sandboxProcessor.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SandboxProcessorKlarnaBrandSandbox {
    private lateinit var klarnaBrand: SandboxProcessorKlarnaBrand

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        klarnaBrand = SandboxProcessorKlarnaBrand()
    }

    @Test
    fun `iconResId should return correct dark icon resource`() {
        assertEquals(R.drawable.ic_logo_klarna_dark, klarnaBrand.iconResId)
    }

    @Test
    fun `logoResId should return correct square logo resource`() {
        assertEquals(R.drawable.ic_logo_klarna_square, klarnaBrand.logoResId)
    }

    @Test
    fun `iconLightResId should return correct light icon resource`() {
        assertEquals(R.drawable.ic_logo_klarna, klarnaBrand.iconLightResId)
    }
}
