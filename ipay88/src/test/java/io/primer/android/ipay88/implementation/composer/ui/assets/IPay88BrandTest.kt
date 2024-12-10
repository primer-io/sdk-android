package io.primer.android.ipay88.implementation.composer.ui.assets

import io.primer.android.ipay88.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IPay88BrandTest {

    @Test
    fun `iconResId should return correct resource id`() {
        val brand = IPay88Brand()
        assertEquals(R.drawable.ic_logo_credit_card, brand.iconResId)
    }
}
