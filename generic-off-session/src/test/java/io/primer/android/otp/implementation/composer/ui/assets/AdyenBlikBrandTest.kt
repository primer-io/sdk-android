package io.primer.android.otp.implementation.composer.ui.assets

import io.mockk.MockKAnnotations
import io.primer.android.offsession.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdyenBlikBrandTest {
    private lateinit var adyenBlikBrand: AdyenBlikBrand

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        adyenBlikBrand = AdyenBlikBrand()
    }

    @Test
    fun `iconResId should return correct light icon resource`() {
        assertEquals(R.drawable.ic_logo_blik_light, adyenBlikBrand.iconResId)
    }

    @Test
    fun `iconDarkResId should return correct dark icon resource`() {
        assertEquals(R.drawable.ic_logo_blik_dark, adyenBlikBrand.iconDarkResId)
    }
}
