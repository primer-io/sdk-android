package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayconiqBrandTest {
    private val payconiqBrand = PayconiqBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_payconiq, payconiqBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_payconiq_light, payconiqBrand.logoResId, "logoResId does not match")
    }
}
