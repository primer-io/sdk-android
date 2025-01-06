package io.primer.android.phoneNumber.implementation.composer.ui.assets

import io.primer.android.offsession.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MbWayBrandTest {
    private lateinit var mbWayBrand: MbWayBrand

    @BeforeEach
    fun setUp() {
        mbWayBrand = MbWayBrand()
    }

    @Test
    fun `should return correct resource IDs`() {
        assertEquals(R.drawable.ic_logo_mbway_light, mbWayBrand.iconResId)
        assertEquals(R.drawable.ic_logo_mbway_dark, mbWayBrand.iconDarkResId)
    }
}
