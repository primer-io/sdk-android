package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class P24BrandTest {

    private val p24Brand = P24Brand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_p24, p24Brand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_p24_square, p24Brand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_p24_light, p24Brand.iconLightResId, "iconLightResId does not match")
    }
}
