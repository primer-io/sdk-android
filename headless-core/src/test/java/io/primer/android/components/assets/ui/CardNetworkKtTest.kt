package io.primer.android.components.assets.ui

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.displayMetadata.domain.model.ImageColor
import io.primer.android.headlessCore.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CardNetworkKtTest {
    @Test
    fun `getCardImageAsset should return specific image asset when available`() {
        mockkStatic("io.primer.android.components.assets.ui.CardNetworkKt")
        val brand = mockk<Brand>()
        val imageColor = mockk<ImageColor>()
        val cardNetworkType = mockk<CardNetwork.Type>()
        every { cardNetworkType.getCardBrand() } returns brand
        every { brand.getImageAsset(imageColor) } returns 123

        val result = cardNetworkType.getCardImageAsset(imageColor)

        assertEquals(123, result)
        verify {
            cardNetworkType.getCardBrand()
            brand.getImageAsset(imageColor)
        }
        unmockkStatic("io.primer.android.components.assets.ui.CardNetworkKt")
    }

    @Test
    fun `getCardImageAsset should return generic card drawable when image asset is not available`() {
        mockkStatic("io.primer.android.components.assets.ui.CardNetworkKt")
        val brand = mockk<Brand>()
        val imageColor = mockk<ImageColor>()
        val cardNetworkType = mockk<CardNetwork.Type>()
        every { cardNetworkType.getCardBrand() } returns brand
        every { brand.getImageAsset(imageColor) } returns null

        val result = cardNetworkType.getCardImageAsset(imageColor)

        assertEquals(R.drawable.ic_generic_card, result)
        verify {
            cardNetworkType.getCardBrand()
            brand.getImageAsset(imageColor)
        }
        unmockkStatic("io.primer.android.components.assets.ui.CardNetworkKt")
    }
}
