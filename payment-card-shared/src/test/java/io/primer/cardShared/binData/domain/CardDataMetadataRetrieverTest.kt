package io.primer.cardShared.binData.domain

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.cardShared.CardNumberFormatter
import io.primer.android.components.domain.core.models.card.PrimerCardData
import io.primer.android.components.domain.core.models.card.PrimerCardMetadata
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CardDataMetadataRetrieverTest {

    // Mocking CardNumberFormatter using MockK
    private val cardNumberFormatterMock = mockk<CardNumberFormatter>()

    // System under test
    private lateinit var retriever: CardDataMetadataRetriever

    @BeforeEach
    fun setUp() {
        retriever = CardDataMetadataRetriever()
        mockkObject(CardNumberFormatter.Companion)

        // Mock the static method CardNumberFormatter.fromString
        every { CardNumberFormatter.fromString(any(), replaceInvalid = false) } returns cardNumberFormatterMock
    }

    @Test
    fun `retrieveMetadata should return correct card type for valid input`() = runBlocking {
        // Prepare test data
        val inputCardNumber = "4111111111111111" // Example Visa card number
        val cardType = CardNetwork.Type.VISA
        every { cardNumberFormatterMock.getCardType() } returns cardType

        val inputData = mockk<PrimerCardData> {
            every { cardNumber } returns inputCardNumber
        }
        val expectedMetadata = PrimerCardMetadata(cardType)

        // Call the method under test
        val result = retriever.retrieveMetadata(inputData)

        // Assert the result
        assertEquals(expectedMetadata, result)
    }
}
