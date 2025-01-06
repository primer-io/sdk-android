package io.primer.android.card.implementation.composer.ui.assets

import io.primer.android.card.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardBrandTest {
    private lateinit var cardBrand: CardBrand

    @BeforeEach
    fun setUp() {
        cardBrand = CardBrand()
    }

    @Test
    fun `iconResId should return correct drawable resource ID`() {
        // Act
        val iconResId = cardBrand.iconResId

        // Assert
        assertEquals(R.drawable.ic_logo_credit_card, iconResId)
    }
}
