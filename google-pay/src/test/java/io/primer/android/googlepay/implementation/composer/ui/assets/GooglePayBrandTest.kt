package io.primer.android.googlepay.implementation.composer.ui.assets

import io.primer.android.data.settings.GooglePayButtonStyle
import io.primer.android.googlepay.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GooglePayBrandTest {

    @Test
    fun `iconResId should return correct resource ID`() {
        // Given
        val brand = GooglePayBrand(GooglePayButtonStyle.BLACK)

        // When
        val iconResId = brand.iconResId

        // Then
        assertEquals(R.drawable.ic_logo_googlepay, iconResId)
    }

    @Test
    fun `logoResId should return correct resource ID when GooglePayButtonStyle is BLACK`() {
        // Given
        val brand = GooglePayBrand(GooglePayButtonStyle.BLACK)

        // When
        val logoResId = brand.logoResId

        // Then
        assertEquals(R.drawable.ic_logo_google_pay_black_square, logoResId)
    }

    @Test
    fun `logoResId should return correct resource ID when GooglePayButtonStyle is WHITE`() {
        // Given
        val brand = GooglePayBrand(GooglePayButtonStyle.WHITE)

        // When
        val logoResId = brand.logoResId

        // Then
        assertEquals(R.drawable.ic_logo_google_pay_square, logoResId)
    }

    @Test
    fun `iconLightResId should return correct resource ID`() {
        // Given
        val brand = GooglePayBrand(GooglePayButtonStyle.BLACK)

        // When
        val iconLightResId = brand.iconLightResId

        // Then
        assertEquals(R.drawable.ic_logo_googlepay_light, iconLightResId)
    }
}
