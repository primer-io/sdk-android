package io.primer.android.webredirect.implementation.composer.ui.assets

import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PoliBrandTest {
    private val poliBrand = PoliBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_poli_dark, poliBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_poli_light, poliBrand.iconLightResId, "iconLightResId does not match")
        assertEquals(R.drawable.ic_logo_poli_dark, poliBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
