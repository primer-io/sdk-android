package io.primer.cardShared.binData.domain

import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.configuration.data.model.CardNetwork
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CardBinMetadataTest {
    @Test
    fun `toSortedPrimerCardNetworks should return ordered list based on allowedCardNetworks`() {
        val metadataList =
            listOf(
                CardBinMetadata("Visa", CardNetwork.Type.VISA),
                CardBinMetadata("Mastercard", CardNetwork.Type.MASTERCARD),
                CardBinMetadata("Amex", CardNetwork.Type.AMEX),
            )
        val allowedCardNetworks =
            listOf(
                CardNetwork.Type.MASTERCARD,
                CardNetwork.Type.VISA,
            )

        val result = metadataList.toSortedPrimerCardNetworks(allowedCardNetworks)

        val expected =
            listOf(
                PrimerCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard", true),
                PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", true),
                PrimerCardNetwork(CardNetwork.Type.AMEX, "Amex", false),
            )

        assertEquals(expected, result)
    }

    @Test
    fun `toSortedPrimerCardNetworks should handle empty metadata list`() {
        val metadataList = emptyList<CardBinMetadata>()
        val allowedCardNetworks = listOf(CardNetwork.Type.VISA)

        val result = metadataList.toSortedPrimerCardNetworks(allowedCardNetworks)

        assertEquals(emptyList<PrimerCardNetwork>(), result)
    }

    @Test
    fun `toSortedPrimerCardNetworks should handle empty allowedCardNetworks list`() {
        val metadataList =
            listOf(
                CardBinMetadata("Visa", CardNetwork.Type.VISA),
                CardBinMetadata("Mastercard", CardNetwork.Type.MASTERCARD),
            )
        val allowedCardNetworks = emptyList<CardNetwork.Type>()

        val result = metadataList.toSortedPrimerCardNetworks(allowedCardNetworks)

        val expected =
            listOf(
                PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", false),
                PrimerCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard", false),
            )

        assertEquals(expected, result)
    }

    @Test
    fun `toSortedPrimerCardNetworks should return unordered unallowed networks`() {
        val metadataList =
            listOf(
                CardBinMetadata("Visa", CardNetwork.Type.VISA),
                CardBinMetadata("Mastercard", CardNetwork.Type.MASTERCARD),
                CardBinMetadata("Amex", CardNetwork.Type.AMEX),
                CardBinMetadata("Discover", CardNetwork.Type.DISCOVER),
            )
        val allowedCardNetworks = listOf(CardNetwork.Type.VISA)

        val result = metadataList.toSortedPrimerCardNetworks(allowedCardNetworks)

        val expected =
            listOf(
                PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", true),
                PrimerCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard", false),
                PrimerCardNetwork(CardNetwork.Type.AMEX, "Amex", false),
                PrimerCardNetwork(CardNetwork.Type.DISCOVER, "Discover", false),
            )

        assertEquals(expected, result)
    }

    @Test
    fun `toSortedPrimerCardNetworks should ignore null networks`() {
        val metadataList =
            listOf(
                CardBinMetadata("Visa", CardNetwork.Type.VISA),
                CardBinMetadata("Mastercard", null),
                CardBinMetadata("Amex", CardNetwork.Type.AMEX),
            )
        val allowedCardNetworks = listOf(CardNetwork.Type.AMEX, CardNetwork.Type.VISA)

        val result = metadataList.toSortedPrimerCardNetworks(allowedCardNetworks)

        val expected =
            listOf(
                PrimerCardNetwork(CardNetwork.Type.AMEX, "Amex", true),
                PrimerCardNetwork(CardNetwork.Type.VISA, "Visa", true),
            )

        assertEquals(expected, result)
    }
}
