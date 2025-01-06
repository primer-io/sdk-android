package io.primer.android.webredirect.implementation.composer.ui.assets

import io.mockk.junit5.MockKExtension
import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BanContactBrandTest {
    private val banContactBrand = BanContactBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_bancontact, banContactBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_bancontact_square, banContactBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_bancontact_dark, banContactBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
