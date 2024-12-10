package io.primer.android.banks.implementation.composer.ui.assets

import io.primer.android.banks.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DotpayBrandTest {

    @Test
    fun `test DotpayBrand resource IDs`() {
        val dotpayBrand = DotpayBrand()

        assertEquals(R.drawable.ic_logo_dotpay_dark, dotpayBrand.iconResId)
        assertEquals(R.drawable.ic_logo_dotpay_light, dotpayBrand.iconLightResId)
    }
}
