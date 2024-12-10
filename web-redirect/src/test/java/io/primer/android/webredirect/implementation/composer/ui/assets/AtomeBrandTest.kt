package io.primer.android.webredirect.implementation.composer.ui.assets

import io.mockk.junit5.MockKExtension
import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AtomeBrandTest {

    private val atomeBrand = AtomeBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_atome, atomeBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_logo_atome_square, atomeBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_logo_atome_dark, atomeBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
