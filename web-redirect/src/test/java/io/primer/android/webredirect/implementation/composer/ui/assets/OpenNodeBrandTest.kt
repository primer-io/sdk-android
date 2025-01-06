package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OpenNodeBrandTest {
    private val openNodeBrand = OpenNodeBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_opennode_logo, openNodeBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_opennode_logo_square, openNodeBrand.logoResId, "logoResId does not match")
    }
}
