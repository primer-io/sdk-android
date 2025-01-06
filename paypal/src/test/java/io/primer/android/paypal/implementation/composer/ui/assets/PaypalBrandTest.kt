package io.primer.android.paypal.implementation.composer.ui.assets

import io.primer.android.paypal.R
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PaypalBrandTest {
    @Test
    fun `iconResId should return PayPal logo drawable resource ID`() {
        // Arrange
        val paypalBrand = PaypalBrand()

        // Act
        val iconResId = paypalBrand.iconResId

        // Assert
        assertEquals(R.drawable.ic_logo_paypal, iconResId)
    }
}
