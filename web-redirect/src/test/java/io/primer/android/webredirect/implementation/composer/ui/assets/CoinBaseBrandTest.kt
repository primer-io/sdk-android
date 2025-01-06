package io.primer.android.webredirect.implementation.composer.ui.assets

import io.mockk.junit5.MockKExtension
import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CoinBaseBrandTest {
    private val coinBaseBrand = CoinBaseBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_coinbase_logo, coinBaseBrand.iconResId, "iconResId does not match")
        assertEquals(R.drawable.ic_coinbase_logo_square, coinBaseBrand.logoResId, "logoResId does not match")
        assertEquals(R.drawable.ic_coinbase_logo_dark, coinBaseBrand.iconDarkResId, "iconDarkResId does not match")
    }
}
