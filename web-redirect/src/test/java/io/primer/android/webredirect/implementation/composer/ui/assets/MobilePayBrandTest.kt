package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MobilePayBrandTest {
    private val mobilePayBrand = MobilePayBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_mobilepay, mobilePayBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_mobilepay_square, mobilePayBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_mobilepay_light, mobilePayBrand.iconLightResId, "iconLightResId does not match")
    }
}
