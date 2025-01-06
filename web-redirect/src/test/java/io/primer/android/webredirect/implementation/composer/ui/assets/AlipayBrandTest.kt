package io.primer.android.webredirect.implementation.composer.ui.assets

import io.mockk.junit5.MockKExtension
import io.primer.android.webredirect.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AlipayBrandTest {
    private val alipayBrand = AlipayBrand()

    @Test
    fun `should return correct resource ids`() {
        assertEquals(R.drawable.ic_logo_alipay, alipayBrand.iconResId)
        assertEquals(R.drawable.ic_logo_alipay_square, alipayBrand.logoResId)
        assertEquals(R.drawable.ic_logo_alipay_light, alipayBrand.iconLightResId)
    }
}
