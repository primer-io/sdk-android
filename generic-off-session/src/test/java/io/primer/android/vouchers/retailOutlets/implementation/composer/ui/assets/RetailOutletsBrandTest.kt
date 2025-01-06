package io.primer.android.vouchers.retailOutlets.implementation.composer.ui.assets

import io.primer.android.offsession.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RetailOutletsBrandTest {
    private lateinit var retailOutletsBrand: RetailOutletsBrand

    @BeforeEach
    fun setUp() {
        retailOutletsBrand = RetailOutletsBrand()
    }

    @Test
    fun `should return correct resource IDs`() {
        assertEquals(R.drawable.ic_retail_outlets, retailOutletsBrand.iconResId)
        assertEquals(R.drawable.ic_retail_outlets_light, retailOutletsBrand.iconLightResId)
    }
}
