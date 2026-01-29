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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
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
    fun `networkSelection should return selectable networks as available`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = listOf(visa, mastercard),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )

        setFetchedMetadata(mockMetadata)

        val result = useCase.networkSelection.first()
        assertEquals(2, result.availableNetworks.size)
        assertTrue(result.availableNetworks.any { it.network == CardNetwork.Type.VISA })
        assertTrue(result.availableNetworks.any { it.network == CardNetwork.Type.MASTERCARD })
        assertTrue(result.isNetworkSelectable)
    }

    @Test
    fun `networkSelection should use detected networks when selectable is null and multiple detected`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = null,
            detectedNetworks = listOf(visa, mastercard),
            preferredNetwork = visa,
        )

        setFetchedMetadata(mockMetadata)

        val result = useCase.networkSelection.first()
        assertEquals(2, result.availableNetworks.size)
        assertFalse(result.isNetworkSelectable)
    }

    @Test
    fun `selectedNetwork should be first detected when selectableNetworks is null`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = null,
            detectedNetworks = listOf(visa, mastercard),
            preferredNetwork = visa,
        )

        setFetchedMetadata(mockMetadata)

        val result = useCase.networkSelection.first()
        assertEquals(visa, result.selectedNetwork)
        assertFalse(result.isNetworkSelectable)
    }

    @Test
    fun `selectedNetwork should be null when selectableNetworks is empty`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mockMetadata = createMetadata(
            selectableNetworks = emptyList(),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )

        setFetchedMetadata(mockMetadata)

        val result = useCase.networkSelection.first()
        assertNull(result.selectedNetwork)
    }

    @Test
    fun `selectedNetwork should default to first network when selectable is non-empty`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = listOf(visa, mastercard),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )

        setFetchedMetadata(mockMetadata)

        val result = useCase.networkSelection.first()
        assertEquals(visa, result.selectedNetwork)
    }

    @Test
    fun `selectCardNetwork should update selected network`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val mockMetadata = createMetadata(
            selectableNetworks = listOf(visa, mastercard),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )
        setFetchedMetadata(mockMetadata)

        useCase.selectCardNetwork(mastercard)

        val result = useCase.networkSelection.first()
        assertEquals(mastercard, result.selectedNetwork)
    }

    @Test
    fun `selected network should fallback to first available if not in list`() = runTest {
        val visa = createCardNetwork(CardNetwork.Type.VISA, "Visa")
        val mastercard = createCardNetwork(CardNetwork.Type.MASTERCARD, "Mastercard")
        val amex = createCardNetwork(CardNetwork.Type.AMEX, "Amex")

        val initialMetadata = createMetadata(
            selectableNetworks = listOf(visa, amex),
            detectedNetworks = listOf(visa),
        )
        setFetchedMetadata(initialMetadata)

        useCase.selectCardNetwork(amex)

        val newMetadata = createMetadata(
            selectableNetworks = listOf(visa, mastercard),
            detectedNetworks = listOf(visa),
            preferredNetwork = visa,
        )
        setFetchedMetadata(newMetadata)

        val result = useCase.networkSelection.first()
        assertEquals(visa, result.selectedNetwork)
    }
}
