package io.primer.android.internal.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.core.models.card.PrimerCardMetadataState
import io.primer.android.components.domain.core.models.card.PrimerCardNetwork
import io.primer.android.components.domain.core.models.card.PrimerCardNetworksMetadata
import io.primer.android.components.domain.core.models.card.PrimerCardNumberEntryMetadata
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.internal.domain.repositories.RawDataManagerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardNetworkUseCaseTest {

    private lateinit var useCase: CardNetworkUseCase
    private lateinit var mockRawDataManagerRepository: RawDataManagerRepository
    private val metadataStateFlow = MutableStateFlow<PrimerCardMetadataState>(
        PrimerCardMetadataState.Fetching(
            cardNumberEntryState = mockk(),
        ),
    )

    @BeforeEach
    fun setUp() {
        mockRawDataManagerRepository = mockk()
        every { mockRawDataManagerRepository.metadataState } returns metadataStateFlow
        useCase = CardNetworkUseCase(mockRawDataManagerRepository)
    }

    private fun createCardNetwork(type: CardNetwork.Type, name: String) =
        PrimerCardNetwork(type, name, allowed = true)

    private fun createMetadata(
        selectableNetworks: List<PrimerCardNetwork>? = null,
        detectedNetworks: List<PrimerCardNetwork> = emptyList(),
        preferredNetwork: PrimerCardNetwork? = null,
    ): PrimerCardNumberEntryMetadata = mockk {
        every { selectableCardNetworks } returns selectableNetworks?.let {
            PrimerCardNetworksMetadata(items = it, preferred = preferredNetwork ?: it.firstOrNull())
        }
        every { detectedCardNetworks } returns PrimerCardNetworksMetadata(
            items = detectedNetworks,
            preferred = preferredNetwork ?: detectedNetworks.firstOrNull(),
        )
        every { source } returns ValidationSource.REMOTE
    }

    private fun setFetchedMetadata(metadata: PrimerCardNumberEntryMetadata) {
        metadataStateFlow.value = PrimerCardMetadataState.Fetched(
            cardNumberEntryMetadata = metadata,
            cardNumberEntryState = mockk(),
        )
    }

    @Test
    fun `detectCardNetwork should update detected card network`() = runTest {
        // When - using known card patterns
        useCase.detectCardNetwork("4111111111111111") // Visa starts with 4

        // Then
        val result = useCase.currentCardNetwork.first()
        assertEquals(CardNetwork.Type.VISA, result)
    }

    @Test
    fun `selectCardNetwork should update selected network`() = runTest {
        // When
        useCase.selectCardNetwork(CardNetwork.Type.AMEX)

        // Then
        val result = useCase.currentCardNetwork.first()
        assertEquals(CardNetwork.Type.AMEX, result)
    }

    @Test
    fun `clear should reset selected and detected networks`() = runTest {
        // Given
        useCase.detectCardNetwork("4111111111111111") // Visa
        useCase.selectCardNetwork(CardNetwork.Type.MASTERCARD)

        // When
        useCase.clear()

        // Then
        val result = useCase.currentCardNetwork.first()
        assertEquals(CardNetwork.Type.OTHER, result)
    }

    @Test
    fun `currentCardNetwork should prefer selected over detected network`() = runTest {
        // When
        useCase.detectCardNetwork("4111111111111111") // Visa
        useCase.selectCardNetwork(CardNetwork.Type.MASTERCARD)

        // Then
        val result = useCase.currentCardNetwork.first()
        assertEquals(CardNetwork.Type.MASTERCARD, result)
    }

    @Test
    fun `availableNetworks should return networks from metadata`() = runTest {
        // Given
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = listOf(visa, mastercard),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )

        // When
        setFetchedMetadata(mockMetadata)

        // Then
        val result = useCase.availableNetworks.first()
        assertEquals(2, result.size)
        assertTrue(result.any { it.network == CardNetwork.Type.VISA })
        assertTrue(result.any { it.network == CardNetwork.Type.MASTERCARD })
    }

    @Test
    fun `availableNetworks should use detected network when selectable is null`() = runTest {
        // Given
        val amex = createCardNetwork(CardNetwork.Type.AMEX, "Amex")
        val mockMetadata = createMetadata(
            selectableNetworks = null,
            detectedNetworks = listOf(amex),
            preferredNetwork = amex,
        )

        // When
        setFetchedMetadata(mockMetadata)

        // Then
        val result = useCase.availableNetworks.first()
        assertEquals(1, result.size)
        assertEquals(CardNetwork.Type.AMEX, result.first().network)
    }

    @Test
    fun `availableNetworks should clear selected network if not in available list`() = runTest {
        // Given
        useCase.selectCardNetwork(CardNetwork.Type.DISCOVER)

        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = listOf(visa, mastercard),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )

        // When
        setFetchedMetadata(mockMetadata)
        useCase.availableNetworks.first()

        // Then - selected network should be cleared since DISCOVER is not in available networks
        val currentNetwork = useCase.currentCardNetwork.first()
        assertEquals(CardNetwork.Type.OTHER, currentNetwork)
    }
}
