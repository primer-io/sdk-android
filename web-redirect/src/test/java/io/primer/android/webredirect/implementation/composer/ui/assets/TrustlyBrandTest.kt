package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrustlyBrandTest {

    private val trustlyBrand = TrustlyBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_trusly, trustlyBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_trustly_square, trustlyBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_trustly_light, trustlyBrand.iconLightResId, "iconLightResId does not match")
    }
}
