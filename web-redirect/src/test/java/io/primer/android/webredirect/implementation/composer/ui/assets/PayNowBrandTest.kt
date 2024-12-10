package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayNowBrandTest {

    private val payNowBrand = PayNowBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_xfers, payNowBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_xfers_square, payNowBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_xfers_light, payNowBrand.iconLightResId, "iconLightResId does not match")
    }
}
