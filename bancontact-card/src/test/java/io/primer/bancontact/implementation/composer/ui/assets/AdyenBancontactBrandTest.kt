package io.primer.bancontact.implementation.composer.ui.assets

import io.primer.android.bancontact.implementation.composer.ui.assets.AdyenBancontactBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.primer.android.bancontact.R

internal class AdyenBancontactBrandTest {

    private lateinit var adyenBancontactBrand: AdyenBancontactBrand

    @BeforeEach
    fun setUp() {
        adyenBancontactBrand = AdyenBancontactBrand()
    }

    @Test
    fun `iconResId should return correct drawable resource ID`() {
        // Act
        val iconResId = adyenBancontactBrand.iconResId

        // Assert
        assertEquals(R.drawable.ic_logo_bancontact, iconResId)
    }

    @Test
    fun `logoResId should return correct drawable resource ID`() {
        // Act
        val logoResId = adyenBancontactBrand.logoResId

        // Assert
        assertEquals(R.drawable.ic_logo_bancontact_square, logoResId)
    }

    @Test
    fun `iconDarkResId should return correct drawable resource ID`() {
        // Act
        val iconDarkResId = adyenBancontactBrand.iconDarkResId

        // Assert
        assertEquals(R.drawable.ic_logo_bancontact_dark, iconDarkResId)
    }
}
