package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VippsBrandTest {

    private val brand = VippsBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_vipps, brand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_vipps_square, brand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_vipps_light, brand.iconLightResId, "iconLightResId does not match")
        assertEquals(R.drawable.ic_logo_vipps_light, brand.iconDarkResId, "iconDarkResId does not match")
    }
}
