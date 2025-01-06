package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayShopBrandTest {
    private val payShopBrand = PayShopBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_payshop_dark, payShopBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_payshop_square, payShopBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_payshop_light, payShopBrand.iconLightResId, "iconLightResId does not match")
    }
}
