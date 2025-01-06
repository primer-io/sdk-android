package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PayTrailBrandTest {
    private val payTrailBrand = PayTrailBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_paytrail, payTrailBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_paytrail_square, payTrailBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_paytrail_light, payTrailBrand.iconLightResId, "iconLightResId does not match")
    }
}
