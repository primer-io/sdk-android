package io.primer.cardShared.binData.domain

import io.mockk.mockk
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CardMetadataCacheHelperTest {

    private lateinit var cacheHelper: CardMetadataCacheHelper

    @BeforeEach
    fun setUp() {
        cacheHelper = CardMetadataCacheHelper()
    }

    @Test
    fun `getCardNetworksMetadata should return null when metadata is not saved`() {
        // Prepare test data
        val bin = "123456"

        // Call the method under test
        val result = cacheHelper.getCardNetworksMetadata(bin)

        // Assert the result
        assertEquals(null, result)
    }

    @Test
    fun `getCardNetworksMetadata should return saved metadata`() {
        // Prepare test data
        val binStr = "123456"
        val metadata = mockk<PrimerCardNumberEntryMetadata>()

        // Save metadata in the cache
        cacheHelper.saveCardNetworksMetadata(binStr, metadata)

        // Call the method under test
        val result = cacheHelper.getCardNetworksMetadata(binStr)

        // Assert the result
        assertEquals(metadata, result)
    }
}
