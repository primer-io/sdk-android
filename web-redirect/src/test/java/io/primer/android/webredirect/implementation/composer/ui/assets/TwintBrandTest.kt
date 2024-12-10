package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TwintBrandTest {

    private val twintBrand = TwintBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_twint, twintBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_twint_square, twintBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_twint_light, twintBrand.iconLightResId, "iconLightResId does not match")
    }
}
