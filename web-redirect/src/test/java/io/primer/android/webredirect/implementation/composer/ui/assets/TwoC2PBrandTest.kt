package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TwoC2PBrandTest {
    private val brand = TwoC2PBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_2c2p_logo, brand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_2c2p_logo_square, brand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_2c2p_logo_dark, brand.iconDarkResId, "iconDarkResId does not match")
    }
}
