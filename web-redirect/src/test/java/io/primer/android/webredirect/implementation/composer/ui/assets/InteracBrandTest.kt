package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InteracBrandTest {
    private val interacBrand = InteracBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_interac, interacBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_interac_square, interacBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_interac_dark, interacBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
