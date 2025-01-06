package io.primer.android.banks.implementation.composer.ui.assets

import io.primer.android.banks.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdealBrandTest {
    @Test
    fun `test IdealBrand resource IDs`() {
        val idealBrand = IdealBrand()

        assertEquals(R.drawable.ic_logo_ideal, idealBrand.iconResId)
        assertEquals(R.drawable.ic_logo_ideal_square, idealBrand.logoResId)
        assertEquals(R.drawable.ic_logo_ideal_light, idealBrand.iconLightResId)
        assertEquals(R.drawable.ic_logo_ideal_dark, idealBrand.iconDarkResId)
    }
}
