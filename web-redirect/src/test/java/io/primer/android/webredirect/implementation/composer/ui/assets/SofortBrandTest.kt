package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SofortBrandTest {

    private val sofortBrand = SofortBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_sofort, sofortBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_sofort_square, sofortBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_sofort_light, sofortBrand.iconLightResId, "iconLightResId does not match")
    }
}
